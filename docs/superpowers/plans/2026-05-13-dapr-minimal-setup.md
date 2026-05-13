# Dapr Minimal Setup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Overlay a minimal Dapr service-invocation setup onto `sclera-cloud-device-asset` so that the main Spring Boot app can call a dummy `vdms-service` via the Dapr Java SDK, validating Dapr connectivity end-to-end.

**Architecture:** Two Spring Boot 2.6.5/Java 11 services each get a Dapr sidecar via `network_mode: service:<name>` in docker-compose. The main app uses `DaprClient.invokeMethod(...)` (from `dapr-sdk-springboot:1.10.0`) to call `vdms-service`. Sidecars discover each other via `nameresolution.dns` using Docker DNS. No commits — file changes only.

**Tech Stack:** Spring Boot 2.6.5, Java 11, Dapr Java SDK 1.10.0 (`dapr-sdk-springboot`), Dapr runtime 1.12.0 (`daprio/daprd`), Docker Compose

**Pre-requisite:** T15 compile loop (main app) must be green (`mvn clean package -DskipTests` succeeds) before running `docker compose up`. The `vdms-service` (Task 5) can be built and tested independently at any time.

---

## File Map

| File | Action | Task |
|---|---|---|
| `pom.xml` | modify — add Dapr SDK dep | T1 |
| `src/main/java/io/sclera/config/WebSecurityConfig.java` | replace — permitAll() | T2 |
| `src/main/java/io/sclera/config/JwtRequestFilter.java` | modify — remove `@Component` | T2 |
| `src/main/java/io/sclera/controller/dapr/DaprTestController.java` | create | T3 |
| `dapr/config.yaml` | create | T4 |
| `dapr/components/nameresolution.yaml` | create | T4 |
| `vdms-service/pom.xml` | create | T5 |
| `vdms-service/Dockerfile` | create | T5 |
| `vdms-service/src/main/java/io/sclera/vdms/VdmsServiceApplication.java` | create | T5 |
| `vdms-service/src/main/java/io/sclera/vdms/VdmsController.java` | create | T5 |
| `vdms-service/src/main/resources/application.yml` | create | T5 |
| `Dockerfile` | modify — fix JAR name | T6 |
| `docker-compose.yml` | replace — add Dapr sidecars, fix env | T7 |

---

## Task 1: Add Dapr SDK to main app pom.xml

**Files:**
- Modify: `pom.xml`

The `dapr-sdk-springboot` starter auto-configures a `DaprClient` bean. The `webflux` dep (already present in pom) satisfies the SDK's reactor requirement.

- [ ] **Step 1: Add Dapr dependency**

Open `pom.xml`. Inside the `<dependencies>` block, add this entry directly before the closing `</dependencies>` tag (line 443):

```xml
        <dependency>
            <groupId>io.dapr</groupId>
            <artifactId>dapr-sdk-springboot</artifactId>
            <version>1.10.0</version>
        </dependency>
```

- [ ] **Step 2: Verify the addition**

Run:
```powershell
Select-String -Path "pom.xml" -Pattern "dapr-sdk-springboot"
```
Expected output: one match showing `dapr-sdk-springboot`.

---

## Task 2: Simplify security — replace WebSecurityConfig, de-annotate JwtRequestFilter

**Files:**
- Replace: `src/main/java/io/sclera/config/WebSecurityConfig.java`
- Modify: `src/main/java/io/sclera/config/JwtRequestFilter.java`

The existing `WebSecurityConfig` wires OAuth2/Keycloak and registers `JwtRequestFilter` as a Spring Security filter. For the Dapr PoC we disable auth globally. `JwtRequestFilter` is `@Component` — if left annotated, Spring will try to autowire `UserService`/`UserActionLogService` at startup, which may not survive the compile loop. Removing `@Component` is safe because the new `WebSecurityConfig` does not register it.

- [ ] **Step 1: Replace WebSecurityConfig.java**

Write the following as the complete content of `src/main/java/io/sclera/config/WebSecurityConfig.java`:

```java
package io.sclera.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
```

- [ ] **Step 2: Remove @Component from JwtRequestFilter**

In `src/main/java/io/sclera/config/JwtRequestFilter.java`, remove the `@Component` annotation on line 26. The line currently reads:

```java
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
```

Change it to:

```java
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
```

- [ ] **Step 3: Verify**

Run:
```powershell
Select-String -Path "src\main\java\io\sclera\config\WebSecurityConfig.java" -Pattern "permitAll"
Select-String -Path "src\main\java\io\sclera\config\JwtRequestFilter.java" -Pattern "@Component"
```
Expected: first command returns one match; second command returns **no** matches.

---

## Task 3: Create DaprTestController

**Files:**
- Create: `src/main/java/io/sclera/controller/dapr/DaprTestController.java`

This controller is isolated — no cross-dependencies with the rest of the codebase. It exposes `GET /dapr/test` which calls `vdms-service` via Dapr SDK.

- [ ] **Step 1: Create the directory**

```powershell
New-Item -ItemType Directory -Force "src\main\java\io\sclera\controller\dapr"
```

- [ ] **Step 2: Create DaprTestController.java**

Write the following as the complete content of `src/main/java/io/sclera/controller/dapr/DaprTestController.java`:

```java
package io.sclera.controller.dapr;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return result != null ? new String(result) : "{}";
    }
}
```

- [ ] **Step 3: Verify file exists**

```powershell
Test-Path "src\main\java\io\sclera\controller\dapr\DaprTestController.java"
```
Expected: `True`

---

## Task 4: Create Dapr configuration files

**Files:**
- Create: `dapr/config.yaml`
- Create: `dapr/components/nameresolution.yaml`

`nameresolution.dns` resolves Dapr app-ids to Docker DNS hostnames. Because each Dapr sidecar uses `network_mode: service:<app>`, the sidecar shares the app container's network namespace — Docker DNS resolves `vdms-service` to the `vdms-service` container's IP, which is the same IP the vdms-dapr sidecar is bound to.

- [ ] **Step 1: Create dapr directory**

```powershell
New-Item -ItemType Directory -Force "dapr\components"
```

- [ ] **Step 2: Create dapr/config.yaml**

Write the following as the complete content of `dapr/config.yaml`:

```yaml
apiVersion: dapr.io/v1alpha1
kind: Configuration
metadata:
  name: daprconfig
spec:
  tracing:
    samplingRate: "0"
```

- [ ] **Step 3: Create dapr/components/nameresolution.yaml**

Write the following as the complete content of `dapr/components/nameresolution.yaml`:

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

- [ ] **Step 4: Verify**

```powershell
Test-Path "dapr\config.yaml"
Test-Path "dapr\components\nameresolution.yaml"
```
Expected: both `True`.

---

## Task 5: Create vdms-service project

**Files:**
- Create: `vdms-service/pom.xml`
- Create: `vdms-service/Dockerfile`
- Create: `vdms-service/src/main/java/io/sclera/vdms/VdmsServiceApplication.java`
- Create: `vdms-service/src/main/java/io/sclera/vdms/VdmsController.java`
- Create: `vdms-service/src/main/resources/application.yml`

This is a standalone minimal Spring Boot service. No DB, no security, no stubs required. It compiles independently of the main app.

- [ ] **Step 1: Create directories**

```powershell
New-Item -ItemType Directory -Force "vdms-service\src\main\java\io\sclera\vdms"
New-Item -ItemType Directory -Force "vdms-service\src\main\resources"
```

- [ ] **Step 2: Create vdms-service/pom.xml**

Write the following as the complete content of `vdms-service/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.6.5</version>
        <relativePath/>
    </parent>

    <groupId>io.sclera</groupId>
    <artifactId>vdms-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>vdms-service</name>

    <properties>
        <java.version>11</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

    <build>
        <finalName>vdms-service</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: Create VdmsServiceApplication.java**

Write the following as the complete content of `vdms-service/src/main/java/io/sclera/vdms/VdmsServiceApplication.java`:

```java
package io.sclera.vdms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VdmsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VdmsServiceApplication.class, args);
    }
}
```

- [ ] **Step 4: Create VdmsController.java**

Write the following as the complete content of `vdms-service/src/main/java/io/sclera/vdms/VdmsController.java`:

```java
package io.sclera.vdms;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

- [ ] **Step 5: Create vdms-service/src/main/resources/application.yml**

Write the following as the complete content of `vdms-service/src/main/resources/application.yml`:

```yaml
server:
  port: 8086
```

- [ ] **Step 6: Create vdms-service/Dockerfile**

Write the following as the complete content of `vdms-service/Dockerfile`:

```dockerfile
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY target/vdms-service.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

- [ ] **Step 7: Build and smoke-test vdms-service locally**

```powershell
cd vdms-service
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "C:\Users\DhanushVasanth\.tools\apache-maven-3.9.15\bin;$env:JAVA_HOME\bin;$env:Path"
mvn clean package -DskipTests
```

Expected: `BUILD SUCCESS`, file `target/vdms-service.jar` created.

- [ ] **Step 8: Run vdms-service standalone to verify endpoint**

```powershell
Start-Process java -ArgumentList "-jar", "target\vdms-service.jar" -NoNewWindow
Start-Sleep -Seconds 8
Invoke-RestMethod -Uri "http://localhost:8086/getVdmsInfo"
```

Expected response:
```json
{"vdmsId":"vdms-001","name":"VDMS Edge Node","status":"active"}
```

Stop the process after verification:
```powershell
Get-Process java | Stop-Process
```

```powershell
cd ..
```

---

## Task 6: Fix main app Dockerfile JAR name

**Files:**
- Modify: `Dockerfile`

The current `Dockerfile` copies `target/sclera-cloud-device-asset.jar` but `pom.xml` sets `<finalName>sclera</finalName>` — the actual JAR is `target/sclera.jar`. This mismatch causes `docker compose build` to fail.

- [ ] **Step 1: Fix the COPY instruction**

In `Dockerfile`, change line 3 from:

```dockerfile
COPY target/sclera-cloud-device-asset.jar app.jar
```

To:

```dockerfile
COPY target/sclera.jar app.jar
```

- [ ] **Step 2: Verify**

```powershell
Select-String -Path "Dockerfile" -Pattern "COPY target"
```
Expected: `COPY target/sclera.jar app.jar`

---

## Task 7: Update docker-compose.yml

**Files:**
- Replace: `docker-compose.yml`

Adds `app-dapr`, `vdms-service`, `vdms-dapr` containers. Fixes app env vars so the app:
- Runs on HTTP port 8085 (not HTTPS 8887)
- Points to the correct MySQL container and database
- Disables RabbitMQ and Flyway auto-configuration (neither is present in this PoC compose)
- Exposes Dapr sidecar HTTP port 3500 for local curl testing

- [ ] **Step 1: Replace docker-compose.yml**

Write the following as the complete content of `docker-compose.yml`:

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
      SERVER_PORT: "8085"
      SERVER_SSL_ENABLED: "false"
      SPRING_DATASOURCE_URL: "jdbc:mysql://sclera-cloud-device-asset-mysql:3306/sclera_cloud_device_asset"
      SPRING_DATASOURCE_USERNAME: "root"
      SPRING_DATASOURCE_PASSWORD: "root"
      SPRING_AUTOCONFIGURE_EXCLUDE: "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
      DAPR_GRPC_PORT: "50001"
    ports:
      - "8085:8085"
      - "3500:3500"

  app-dapr:
    image: daprio/daprd:1.12.0
    command:
      - "./daprd"
      - "--app-id"
      - "sclera-cloud-device-asset"
      - "--app-port"
      - "8085"
      - "--dapr-http-port"
      - "3500"
      - "--dapr-grpc-port"
      - "50001"
      - "--config"
      - "/dapr/config.yaml"
      - "--components-path"
      - "/dapr/components"
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
    command:
      - "./daprd"
      - "--app-id"
      - "vdms-service"
      - "--app-port"
      - "8086"
      - "--dapr-http-port"
      - "3500"
      - "--dapr-grpc-port"
      - "50001"
      - "--config"
      - "/dapr/config.yaml"
      - "--components-path"
      - "/dapr/components"
    volumes:
      - ./dapr:/dapr
    network_mode: "service:vdms-service"
    depends_on:
      - vdms-service

volumes:
  mysql_data:
```

- [ ] **Step 2: Verify key entries**

```powershell
Select-String -Path "docker-compose.yml" -Pattern "app-dapr|vdms-dapr|nameresolution|DAPR_GRPC_PORT"
```
Expected: matches for `app-dapr`, `vdms-dapr`, `DAPR_GRPC_PORT`.

---

## Task 8: Build and run the full stack

**Pre-condition:** Main app must compile — `mvn clean package -DskipTests` must succeed (T15 complete).

- [ ] **Step 1: Build main app JAR**

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "C:\Users\DhanushVasanth\.tools\apache-maven-3.9.15\bin;$env:JAVA_HOME\bin;$env:Path"
mvn clean package -DskipTests
```
Expected: `BUILD SUCCESS`, file `target/sclera.jar` exists.

- [ ] **Step 2: Start all containers**

```powershell
docker compose up --build
```
Expected in logs (within ~30 seconds):
- `sclera-cloud-device-asset` — `Started ScleraCloudDeviceAssetApplication`
- `vdms-service` — `Started VdmsServiceApplication`
- `app-dapr` — `dapr initialized. Status: Running`
- `vdms-dapr` — `dapr initialized. Status: Running`

- [ ] **Step 3: Test vdms-service directly (bypasses Dapr)**

```powershell
Invoke-RestMethod -Uri "http://localhost:8086/getVdmsInfo"
```
Expected:
```json
{"vdmsId":"vdms-001","name":"VDMS Edge Node","status":"active"}
```

- [ ] **Step 4: Test Dapr SDK invocation through main app**

```powershell
Invoke-RestMethod -Uri "http://localhost:8085/dapr/test"
```
Expected:
```json
{"vdmsId":"vdms-001","name":"VDMS Edge Node","status":"active"}
```

- [ ] **Step 5: Test via main app's Dapr sidecar HTTP port directly**

```powershell
Invoke-RestMethod -Uri "http://localhost:3500/v1.0/invoke/vdms-service/method/getVdmsInfo"
```
Expected:
```json
{"vdmsId":"vdms-001","name":"VDMS Edge Node","status":"active"}
```

All three tests returning the same JSON confirms: vdms-service is up, Dapr sidecars are connected, and the Dapr Java SDK call path works end-to-end.

- [ ] **Step 6: Tear down**

```powershell
docker compose down
```

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `sclera.jar: No such file or directory` during docker build | T15 compile not complete | Complete compile loop first |
| `app-dapr` exits immediately | App container not ready yet | Add `healthcheck` to `app` service or increase `depends_on` delay |
| `GET /dapr/test` returns 500 | Dapr sidecar can't reach vdms-dapr | Check `vdms-dapr` logs; verify `nameresolution.dns` component loaded |
| `Started ... but port already in use` | Port 8085/8086/3306 occupied locally | `docker compose down` then retry |
| Context load fails with `RabbitMQ` error | `SPRING_AUTOCONFIGURE_EXCLUDE` not picked up | Verify env var name matches exactly (no spaces after comma in value) |
| Context load fails with OAuth2 error | `oauth2-resource-server:3.0.6` version skew | Add `OAuth2ResourceServerAutoConfiguration` to `SPRING_AUTOCONFIGURE_EXCLUDE` |
