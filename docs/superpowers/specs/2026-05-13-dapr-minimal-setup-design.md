# Dapr Minimal Setup — sclera-cloud-device-asset

**Status:** Approved  
**Date:** 2026-05-13  
**Repo:** `sclera-cloud-device-asset` (branch `sclera2.0/development`)  
**Authors:** brainstorming session, Claude + backend.amp@sclera.com

---

## 1. Goal

Overlay a minimal Dapr-based inter-service communication setup onto the existing
`sclera-cloud-device-asset` project. The sole objective is to **validate Dapr
connectivity** between the main Spring Boot service and a new dummy `vdms-service`
using the Dapr Java SDK — no REST template HTTP calls between sidecars.

This is a targeted overlay (Option C). The existing `docker-compose.yml`, MySQL,
and Spring Boot app structure are preserved. No Kafka, telemetry, RabbitMQ,
authentication, or extra infra is introduced.

---

## 2. Decisions

| # | Decision | Value |
|---|---|---|
| 1 | Inter-service transport | Dapr Java SDK (`DaprClient.invokeMethod`) — no RestTemplate HTTP between sidecars |
| 2 | SDK dependency | `io.dapr:dapr-sdk-springboot:1.10.0` added to main app `pom.xml` |
| 3 | dummy service stack | Spring Boot 2.6.5 / Java 11 — same stack as main app |
| 4 | Name resolution | `nameresolution.dns` component — Docker DNS resolves app-id to container hostname |
| 5 | Sidecar placement | `network_mode: service:<app>` — sidecar shares network namespace with its app |
| 6 | Auth | `WebSecurityConfig` updated to `permitAll()` globally; `KEYCLOAK_ISSUER_URI` removed from compose |
| 7 | Placement service | Not required — service invocation does not need Dapr actors placement |
| 8 | Tracing / telemetry | Disabled in `dapr/config.yaml` |

---

## 3. Architecture

```
Docker network: sclera-dapr-net
┌────────────────────────────────────────────────────────┐
│                                                        │
│  ┌──────────────────────────────┐                      │
│  │  sclera-cloud-device-asset   │                      │
│  │  Spring Boot  :8085          │                      │
│  │  ┌──────────────────────┐    │                      │
│  │  │ Dapr sidecar         │    │                      │
│  │  │ HTTP :3500           │    │                      │
│  │  │ gRPC :50001          │◄───┼── DaprClient SDK     │
│  │  └──────────┬───────────┘    │                      │
│  └─────────────┼────────────────┘                      │
│                │ Dapr gRPC service invocation           │
│  ┌─────────────▼────────────────┐                      │
│  │  vdms-service                │                      │
│  │  Spring Boot  :8086          │                      │
│  │  ┌──────────────────────┐    │                      │
│  │  │ Dapr sidecar         │    │                      │
│  │  │ HTTP :3500           │    │                      │
│  │  │ gRPC :50001          │    │                      │
│  │  └──────────────────────┘    │                      │
│  └──────────────────────────────┘                      │
│                                                        │
│  ┌─────────────┐                                       │
│  │  MySQL :3306│  (unchanged)                          │
│  └─────────────┘                                       │
└────────────────────────────────────────────────────────┘
```

### Call flow

```
Client
  GET http://localhost:8085/dapr/test
    └─ DaprTestController.test()
         └─ daprClient.invokeMethod(
              "vdms-service",    // app-id
              "getVdmsInfo",     // method
              null,              // no body
              HttpExtension.GET,
              byte[].class
            )
              └─ app sidecar localhost:50001 (gRPC)
                   └─ vdms-service sidecar vdms-service:50001 (Docker DNS)
                        └─ vdms-service app localhost:8086
                             GET /getVdmsInfo
                               └─ { "vdmsId": "vdms-001", "name": "VDMS Edge Node", "status": "active" }
```

### Dapr app-ids

| Service | app-id | app-port |
|---|---|---|
| `sclera-cloud-device-asset` | `sclera-cloud-device-asset` | `8085` |
| `vdms-service` | `vdms-service` | `8086` |

---

## 4. Components

### 4.1 Files modified

| File | Change |
|---|---|
| `docker-compose.yml` | Add `app-dapr`, `vdms-service`, `vdms-dapr` containers; remove `KEYCLOAK_ISSUER_URI` from `app` env |
| `pom.xml` | Add `io.dapr:dapr-sdk-springboot:1.10.0` dependency |
| `src/main/java/io/sclera/config/WebSecurityConfig.java` | Replace OAuth2/Keycloak with `permitAll()` globally |

### 4.2 Files created

| File | Purpose |
|---|---|
| `dapr/config.yaml` | Minimal Dapr config — tracing disabled |
| `dapr/components/nameresolution.yaml` | `nameresolution.dns` for Docker DNS resolution |
| `src/main/java/io/sclera/controller/dapr/DaprTestController.java` | `GET /dapr/test` — calls vdms-service via DaprClient |
| `vdms-service/pom.xml` | Minimal Spring Boot 2.6.5 / Java 11 POM, no DB, no security |
| `vdms-service/Dockerfile` | JRE 11 base image, same pattern as main app |
| `vdms-service/src/main/java/io/sclera/vdms/VdmsServiceApplication.java` | `@SpringBootApplication` entry point |
| `vdms-service/src/main/java/io/sclera/vdms/VdmsController.java` | `GET /getVdmsInfo` → static JSON |
| `vdms-service/src/main/resources/application.yml` | `server.port: 8086` only |

### 4.3 Services removed from docker-compose

None were present. Removed from env only: `KEYCLOAK_ISSUER_URI`.

---

## 5. Data flow

### DaprTestController (main app)

```java
@RestController
@RequestMapping("/dapr")
public class DaprTestController {

    private final DaprClient daprClient;

    public DaprTestController(DaprClient daprClient) {
        this.daprClient = daprClient;
    }

    @GetMapping("/test")
    public String test() {
        byte[] result = daprClient
            .invokeMethod("vdms-service", "getVdmsInfo", null, HttpExtension.GET, byte[].class)
            .block();
        return new String(result);
    }
}
```

### VdmsController (vdms-service)

```java
@RestController
public class VdmsController {

    @GetMapping("/getVdmsInfo")
    public Map<String, String> getVdmsInfo() {
        return Map.of(
            "vdmsId", "vdms-001",
            "name",   "VDMS Edge Node",
            "status", "active"
        );
    }
}
```

---

## 6. Dapr configuration files

### `dapr/config.yaml`

```yaml
apiVersion: dapr.io/v1alpha1
kind: Configuration
metadata:
  name: daprconfig
spec:
  tracing:
    samplingRate: "0"
```

### `dapr/components/nameresolution.yaml`

```yaml
apiVersion: dapr.io/v1alpha1
kind: Component
metadata:
  name: nameresolution
spec:
  type: nameresolution.dns
  version: v1
  metadata: []
```

### `docker-compose.yml` (cleaned, annotated)

```yaml
version: "3.8"
services:

  mysql:
    image: mysql:8
    container_name: sclera-cloud-device-asset-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: sclera_cloud_device_asset
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  app:
    build: .
    container_name: sclera-cloud-device-asset
    depends_on:
      - mysql
    environment:
      DB_USER: root
      DB_PASS: root
      DAPR_GRPC_PORT: "50001"
    ports:
      - "8085:8085"

  app-dapr:
    image: daprio/daprd:1.12.0
    command: [
      "./daprd",
      "--app-id", "sclera-cloud-device-asset",
      "--app-port", "8085",
      "--dapr-http-port", "3500",
      "--dapr-grpc-port", "50001",
      "--config", "/dapr/config.yaml",
      "--components-path", "/dapr/components"
    ]
    volumes:
      - ./dapr:/dapr
    network_mode: "service:app"
    depends_on:
      - app

  vdms-service:
    build: ./vdms-service
    container_name: vdms-service
    ports:
      - "8086:8086"

  vdms-dapr:
    image: daprio/daprd:1.12.0
    command: [
      "./daprd",
      "--app-id", "vdms-service",
      "--app-port", "8086",
      "--dapr-http-port", "3500",
      "--dapr-grpc-port", "50001",
      "--config", "/dapr/config.yaml",
      "--components-path", "/dapr/components"
    ]
    volumes:
      - ./dapr:/dapr
    network_mode: "service:vdms-service"
    depends_on:
      - vdms-service

volumes:
  mysql_data:
```

---

## 7. Startup & run commands

```bash
# 1. Build vdms-service JAR
cd vdms-service
mvn clean package -DskipTests
cd ..

# 2. Build main app JAR (requires T15 compile loop to be green first)
mvn clean package -DskipTests

# 3. Start all containers
docker compose up --build

# 4. Tear down
docker compose down
```

> **Note:** The main app must compile (`mvn clean package` succeeds) before
> `docker compose up --build` can start it. Complete T15 of the compile loop
> before running the full stack.

---

## 8. Test steps

```bash
# Verify vdms-service responds directly (bypasses Dapr)
curl http://localhost:8086/getVdmsInfo
# Expected: {"vdmsId":"vdms-001","name":"VDMS Edge Node","status":"active"}

# Verify Dapr invocation end-to-end through main app
curl http://localhost:8085/dapr/test
# Expected: {"vdmsId":"vdms-001","name":"VDMS Edge Node","status":"active"}

# Verify via main app's Dapr sidecar HTTP directly (sidecar sanity check)
curl http://localhost:3500/v1.0/invoke/vdms-service/method/getVdmsInfo
# Expected: {"vdmsId":"vdms-001","name":"VDMS Edge Node","status":"active"}
```

Postman collection equivalent:
- `GET http://localhost:8086/getVdmsInfo` — vdms-service direct
- `GET http://localhost:8085/dapr/test` — full Dapr SDK invocation path
- `GET http://localhost:3500/v1.0/invoke/vdms-service/method/getVdmsInfo` — sidecar HTTP path

---

## 9. Final architecture summary

**Services kept:**
- `sclera-cloud-device-asset` (Spring Boot :8085) + Dapr sidecar
- `mysql` (unchanged)

**Services added:**
- `vdms-service` (Spring Boot :8086) + Dapr sidecar

**Services removed:** none (existing compose was already clean)

**Removed from env:** `KEYCLOAK_ISSUER_URI`

**Modified files:** `docker-compose.yml`, `pom.xml`, `WebSecurityConfig.java`

**New files:** `dapr/config.yaml`, `dapr/components/nameresolution.yaml`,
`DaprTestController.java`, `vdms-service/` (full directory)

---

## 10. Out of scope

- Dapr pub/sub, state store, bindings — connectivity PoC only
- Dapr actors and placement service
- Re-enabling auth / Keycloak after PoC
- Fixing remaining T15 compile errors (pre-requisite, not part of this design)
- Kubernetes deployment manifests
