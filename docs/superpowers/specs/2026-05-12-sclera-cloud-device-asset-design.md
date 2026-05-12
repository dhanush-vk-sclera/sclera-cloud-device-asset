# sclera-cloud-device-asset — Extraction Design

**Status:** Draft, pending user approval
**Date:** 2026-05-12
**Source repo:** `sclera-vdms-edge-server` (branch `feature/microservice-device-service`) — read-only throughout this work
**New repo:** `Microservice/sclera-cloud-device-asset` (sibling of source; fresh git repo)
**Authors:** brainstorming session, Claude + backend.amp@sclera.com

---

## 1. Goal

Extract the **AP-C1: sclera-cloud-device-asset** slice (15 features defined in `Sclera_decomposition.xlsx`, sheet `V2_DECOMP_MAPPED`, column 10) from `sclera-vdms-edge-server` into a new standalone Spring Boot microservice. This is a **verbatim copy** — same stack, same package root, no modernization. The new repo is a seed for AP-C1 work; production-grade modernization (Spring Boot 3, Java 21, `com.sclera.*`, tenant isolation, real remote calls in place of stubs) is out of scope for Phase 1.

The source repo `sclera-vdms-edge-server` is **read-only** for the entire extraction. No file in the source repo is modified, deleted, renamed, or otherwise touched. All operations are additive on the new repo only.

---

## 2. AP-C1 features in scope

From `V2_DECOMP_MAPPED` sheet 1, column 10:

1. Device CRUD
2. Asset onboarding
3. Asset Onboarding AI (AI-assisted image/spec auto-fill)
4. Asset fields
5. Asset & sensor category taxonomy (global)
6. Asset resources
7. Asset notes
8. Building / floor / location mgmt
9. Digital twin templates — **deferred** (no source-of-truth code exists; only the `Device.digital_twin_image_url` field remains)
10. Device specs
11. Device lifecycle history
12. Device conditions
13. Device / managed asset
14. Device Specification (duplicate of #10)
15. Managed software

---

## 3. Decisions summary

| # | Decision | Value |
|---|---|---|
| 1 | Extraction style | Verbatim copy, same stack (Spring Boot 2.6.5 / Java 11 / `io.sclera.*`) |
| 2 | Dependency scope | AP-C1 + shared infra copied; Bucket-C stubbed; Bucket-D never-copied |
| 3 | Digital twin templates | Skipped (only `Device.digital_twin_image_url` field retained) |
| 4 | Repo location | `Microservice/sclera-cloud-device-asset` (sibling of source) |
| 5 | Maven coordinates | groupId `io.sclera`, artifactId `sclera-cloud-device-asset`, version `0.0.1-SNAPSHOT`, finalName `sclera-cloud-device-asset` |
| 6 | Source repo modifications | None. `sclera-vdms-edge-server` stays read-only |
| 7 | DB | Separate MySQL schema `sclera_cloud_device_asset`; AP-C1 tables only; `hibernate.ddl-auto=update` |
| 8 | URL paths | `/admin/...` paths preserved verbatim (no `/api/v1/...` renaming in Phase 1) |
| 9 | Auth | Copy monolith's Keycloak/OAuth2 config verbatim; point at same issuer |
| 10 | Execution approach | Approach 3 — iterative compile-driven extraction |

Resolved defaults (no explicit user override requested):
- `Customer_Organisation` and the **tenant-level** "Property" entity (customer/billing/contract container) → stubbed (CP-2). `Building/Floor/Location` and the **facility-level** "PropertyService" hierarchy (`PropertyServiceController`, `/admin/property/**`) are AP-C1, copied. The word "property" is overloaded in the source code — only the tenant-level one is stubbed.
- `IntegrationController` and other "owned by another microservice" controllers (Alert*, Workorder*, Inspection*, Inventory*, ADC*, History*, etc.) → never-copied (no endpoints on the new service). Their **service classes** are stubbed in `io.sclera.stubs/` only where AP-C1 code calls them.
- Bucket-D files → never-copied (clean slate in the new repo); no `legacy-reference/` folder.

---

## 4. Architecture

### 4.1 Repo layout

```
Microservice/
├── sclera-vdms-edge-server/        # untouched (read-only source)
└── sclera-cloud-device-asset/      # NEW, fresh git repo
    ├── pom.xml
    ├── mvnw / mvnw.cmd
    ├── Dockerfile
    ├── docker-compose.yml
    ├── README.md
    ├── CLAUDE.md
    ├── migration-notes/
    │   ├── removed-dependencies.md
    │   ├── stubbed-services.md
    │   ├── compile-fixes.md
    │   ├── endpoint-diff.md
    │   ├── known-breakages.md
    │   └── smoke-checklist.md
    └── src/
        ├── main/
        │   ├── java/io/sclera/
        │   │   ├── ScleraCloudDeviceAssetApplication.java
        │   │   ├── config/
        │   │   ├── controller/admin/
        │   │   ├── dto/
        │   │   ├── enums/
        │   │   ├── exception/
        │   │   ├── models/
        │   │   ├── Repository/        # capital R kept verbatim
        │   │   ├── service/
        │   │   ├── stubs/             # NEW — Bucket-C placeholders
        │   │   └── utils/
        │   └── resources/
        │       ├── application.yml
        │       ├── application-test.yml
        │       └── External_jar/ScleraTools.jar    # if linked
        └── test/
            └── java/io/sclera/
                ├── ScleraCloudDeviceAssetApplicationTests.java
                ├── stubs/StubContractTests.java
                ├── controller/admin/ (slice tests, one per feature family)
                └── Repository/DeviceRepositoryTest.java
```

### 4.2 Package and component rules

- **Package root** stays `io.sclera.*` for Phase 1 to preserve `@ComponentScan` and JPA entity scanning. CLAUDE.md's `com.sclera.<app>` rule targets greenfield services and does not apply to this verbatim extraction. Re-packaging is deferred to post-stabilization.
- **`Repository/` directory naming** (capital R) preserved verbatim. Renaming deferred.
- **New package `io.sclera.stubs/`** added for Bucket-C dependencies.

### 4.3 Stub contract

Every class in `io.sclera.stubs/`:

- Mirrors the original class's public method signatures exactly (call sites compile unchanged).
- Annotated `@Service` so Spring autowires it in place of the original bean.
- Returns a safe default per return type:
  - object → `null`
  - `Collection` / `List` / `Set` / `Map` → empty
  - `Optional` → `Optional.empty()`
  - primitive numeric → 0
  - `boolean` → `false`
  - non-optional DTO → no-arg constructor instance with default fields
- Emits exactly one WARN log per invocation:
  `STUB CALL: class={fqn} method={name} target_microservice={ap-cX-name} args_hash={hash}`
- Carries a class-level Javadoc: `// TODO: replace with remote call to <microservice-name>`.
- **Never throws.** Throwing would change observable behavior of the calling AP-C1 service.

### 4.4 New application entry point

```java
package io.sclera;

@SpringBootApplication
public class ScleraCloudDeviceAssetApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScleraCloudDeviceAssetApplication.class, args);
    }
}
```

Same package as the monolith's `ScleraApplication`; only the class name differs. Component scanning works unchanged.

---

## 5. Components

### 5.1 Bucket A — AP-C1 domain code (copied verbatim into new repo)

| Feature | Classes copied (initial seed) |
|---|---|
| Device CRUD, Device/managed asset | DeviceController, MyDevicesController, DeviceService, MyDevicesService, DeviceSearchService, DeviceRepository, Device, AssetDeviceMapping, AssetDeviceMappingRepository, DeviceDTO, DevicesDTO, DeviceDetailDTO, DevicedataDTO, DeviceTopologyDTO, DeviceAlertDTO, DeviceInstalledApps* |
| Asset onboarding | AssetOnboardController, AssetOnboardService, DeviceOnboardStatus, DeviceOnboardStatusAssignee, DeviceOnboardStatusRepository, DeviceOnboardStatusAssigneeService and DTOs |
| Asset Onboarding AI | ChatGPTController, ChatGPTService, ChatGPTDTO, AiCallLogController, AiCallService, AiCallLog, AiCallLogHistory and repos and DTOs, DeviceTechnicianAISuggestionController/Service/Model/Repository/DTO |
| Asset fields | AssetFieldController, AssetField, AssetFieldRepository, AssetFieldDTO |
| Asset and sensor category taxonomy | DeviceTypes, DeviceTypesRepository, DeviceTypeService, DeviceTypesDTO |
| Asset resources | DocumentController, DocumentService, Document, DocumentRepository, DocumentMediaDTO, MediaController, MediaService, Media, MediaRepository |
| Asset notes | NoteController, NotesService (plus Note model/repo/DTO if present) |
| Building/floor/location mgmt | BuildingController/Service/Repository/Model/DTO, FloorController..., LocationController..., LocationHistoryController/Service/Model/Repo/DTO, PropertyServiceController, Address, AddressService, AddressRepository |
| Device specs (x2) | DeviceSpecificationController/Service/Repository/Model/DTO, DeviceNetworkSpecification, DeviceNetworkSpecificationRepository, SpecificationsController |
| Device lifecycle history | DeviceLifecycleHistoryController/Service/Repository/Model/DTO |
| Device conditions | DeviceConditionsController/Service/Repository/Model/DTO |
| Managed software | ManagedSoftwareController/Service/Repository/Model/DTO, ManagedSoftwareSearchService |
| Digital twin templates | Skipped — only the `Device.digital_twin_image_url` field remains |

The compile pass surfaces any sub-helper an above class drags along; new additions classified Bucket B / C / D on the spot, logged in `migration-notes/compile-fixes.md`.

### 5.2 Bucket B — Shared infrastructure (copied; A won't compile/run without it)

- `config/*` — Spring config (Jackson, OpenAPI, JPA, Caffeine cache, Security/JWT). Edge-specific configs (RabbitMQ bindings, WebSocket, network proxies) excluded.
- `exception/GlobalExceptionHandler` plus every custom exception thrown by AP-C1 services.
- `utils/*` — copied class-by-class as the compile pass demands. `APIRequest.java` is in. Methods inside `utils/*` that target other microservices (e.g. `sendToCorrigo()`, `pushToMaximo()`) are trimmed during copy.
- `enums/*` — only enums referenced by AP-C1.
- `dto/ApiResponse.java` and other generic response wrappers.
- `models/ApplicationUser`, `ApplicationUserService`, `ApplicationUserRepository`, plus JWT filter chain.
- `application.yml` — DB block reset to `sclera_cloud_device_asset`; AMQP, edge integration URLs, WebSocket endpoints removed.
- `pom.xml` — derived from monolith's; removals listed in §5.4.
- `Dockerfile` — JRE base image and ENTRYPOINT pattern kept; JAR name updated.

### 5.3 Bucket C — Stubs (new code in `io.sclera.stubs/`)

| Stub class (mirrors original signatures) | Target microservice |
|---|---|
| AlertProfileService, AlertService, AlertDowntimeScheduleService | AP-C5 sclera-cloud-alert/notification |
| IntegrationService, BacnetService, SnmpDeviceService, DaintreeService, DisruptiveService, AwairService, EcobeeService, MonnitService, LorawanService, Lorawanv4Service, ModbusService, KNXService, MqttService, AirthingService, GaiameshService, IPSService, PelicanService, PolyLensService, VergeSenseService, SiemensService | AP-C2 sclera-cloud-sensor-integrations |
| TicketService, WorkorderService, CorrigoService, MaximoService, MriService, MriTaskService, PmsService | AP-C3 sclera-cloud-workorder |
| InspectionRecordService, CheckListRecordService, CheckListTemplateService, GlobalInspectionRecordService, GlobalChecklistService, ReportConditionsService | AP-C4 sclera-cloud-inspection |
| InventoryService, InventoryDeviceService, Product_DetailsService | AP-C8 sclera-cloud-inventory |
| ADCService | AP-C9 sclera-cloud-adc |
| HistoryService, AnalyticsService, UserActionLogService | AP-C6 sclera-cloud-analytics |
| Customer_OrganisationService, country/state/province lookups, Property hierarchy beyond Building/Floor/Location | CP-2 sclera-cp-tenant-management |

Final list determined during Phase 2 compile pass; recorded in `migration-notes/stubbed-services.md`.

### 5.4 Bucket D — Excluded from the copy whitelist (never appear in the new repo)

"Excluded" means: these files are never copied into the new repo. The source repo `sclera-vdms-edge-server` is **not modified** — all files below remain intact there.

Excluded packages (entire subtrees):
- `controller/touchscreen/`, `controller/networktools/`
- `offline/`
- `sockets/`, `websocket/`
- `proxy/`, `rabbitmq/`, `startup/`
- `dto/touchscreen/` (incl. `assetmapper/`, `settings/`, `settings/dockercli/`)
- `service/touchscreen/`
- `integration/` (edge integration sub-app)

Excluded `controller/admin/` files (map to other microservices):
- AP-C2 (sensors): Bacnet*, Snmp*, Daintree*, Disruptive*, Awair*, Ecobee*, Monnit*, Lorawan*, Modbus*, KNX*, Mqtt*, Airthing*, Gaiamesh*, IPS*, Pelican*, PolyLens*, VergeSense*, Siemens*
- AP-C3 (workorder): Ticket*, Workorder*, Corrigo*, Maximo*, Mri*, Pms*
- AP-C4 (inspection): Inspection*, Checklist*, RecordChecklist*, GlobalInspection*, GlobalChecklist*, ReportConditions*
- AP-C5 (alerts): AlertProfile*, AlertDowntimeSchedule*
- AP-C6 (analytics): Analytics*, History*, UserActionLog*
- AP-C8 (inventory): Inventory*
- AP-C9 (ADC): ADC*
- Edge admin: Vlan*, Port*, Profile*, Phonebook*, RemoteAccess*, Sync*, Syslog*, Docker*, JobScheduler*, IOCService, BackupDataService, DataHoistService, MasterSlaveAPICallService, IntelMqttDecoder

Excluded Maven dependencies (removed from new repo's pom.xml):
- `spring-boot-starter-amqp`
- `spring-boot-starter-websocket`
- `spring-boot-starter-thymeleaf`
- `javax.websocket-api`
- `webjars-locator`, `sockjs-client`, `stomp-websocket`, `bootstrap`, `jquery`
- `commons-net`
- `twilio` (verify in compile pass; remove unless AP-C1 actually uses it)

Excluded system jars (kept out of `External_jar/`):
- `lorawan.jar`, `modbus.jar`, `mri.jar`, `haystack-java.jar`

Kept system jar (verified during compile pass):
- `ScleraTools.jar` — likely linked by AP-C1

### 5.5 New artifacts (freshly authored)

- `ScleraCloudDeviceAssetApplication.java`
- `io.sclera.stubs/*` — one class per Bucket-C dependency
- `migration-notes/` folder, 6 files:
  - `removed-dependencies.md` — dropped Maven deps and system jars with reason
  - `stubbed-services.md` — stub classes, target microservices, mirrored signatures
  - `compile-fixes.md` — running log of compile errors and resolutions (B/C/D classification)
  - `endpoint-diff.md` — endpoints kept vs. dropped vs. relocated (for MFE-4 / frontend)
  - `known-breakages.md` — behaviors that compile but don't behave correctly until stubs become remote calls
  - `smoke-checklist.md` — manual verification steps after first boot

---

## 6. Data flow

### 6.1 Database

- MySQL 8, schema `sclera_cloud_device_asset`.
- `hibernate.ddl-auto=update`; schema is generated from copied `@Entity` classes on first boot. No Flyway/Liquibase in Phase 1.
- The settled table set contains AP-C1 entities only — cross-microservice tables (`alert_profile`, `ticket`, `inspection_record`, `integration_*`, `inventory_*`) never get created because their entities are not copied.

### 6.2 HTTP surface

Routes preserved verbatim from monolith:

- `/admin/device/**`, `/admin/my-devices/**`
- `/admin/asset-onboard/**`, `/admin/device-onboard-status/**`
- `/admin/asset-field/**`
- `/admin/device-type/**`
- `/admin/note/**`, `/admin/document/**`, `/admin/media/**`
- `/admin/building/**`, `/admin/floor/**`, `/admin/location/**`, `/admin/location-history/**`, `/admin/property/**`
- `/admin/device-specification/**`, `/admin/specifications/**`, `/admin/device-network-specification/**`
- `/admin/device-lifecycle-history/**`
- `/admin/device-conditions/**`
- `/admin/managed-software/**`
- `/admin/chat-gpt/**`, `/admin/ai-call-log/**`, `/admin/device-technician-ai-suggestion/**`

Endpoints owned by other microservices are not exposed; clients that hit them on the new host receive 404. Mapping recorded in `migration-notes/endpoint-diff.md`.

### 6.3 Stub call flow

```
AssetOnboardService.onboard(...)
   ├─ deviceRepository.save(...)              ← real, AP-C1
   ├─ deviceLifecycleHistoryService.log(...)  ← real, AP-C1
   ├─ alertProfileService.attachDefault(...)  ← STUB (AP-C5), returns null + WARN
   ├─ integrationService.linkSensor(...)      ← STUB (AP-C2), returns empty list + WARN
   └─ historyService.audit(...)               ← STUB (AP-C6), no-op + WARN
```

AP-C1's own data path is intact (Device, AssetField, Onboard status, Lifecycle history all persist correctly). Side-effects belonging to other microservices are visibly skipped via WARN logs.

### 6.4 Auth flow

`spring-boot-starter-oauth2-resource-server` + `java-jwt` + the security configs under `config/` are copied verbatim. Tokens are validated locally against the **same Keycloak issuer** the monolith uses. `ApplicationUser` is resolved from JWT claims and threaded through `createdBy`/`updatedBy` audit fields. Integration with SS-1 `sclera-identity` is a Phase-2 concern.

---

## 7. Error handling

- `GlobalExceptionHandler` (`@RestControllerAdvice`) copied as-is with all its `@ExceptionHandler` methods.
- Response shape stays the existing `ApiResponse<>` wrapper.
- **Stubs never throw.** Each returns a safe default and emits a single WARN log. The HTTP response remains successful for the AP-C1 work; the missing side-effect is visible only in logs and `migration-notes/known-breakages.md`.
- Exception classes thrown by copied code but defined in a Bucket-C/D package are decided per-class during the compile pass:
  - Generic-looking ones (e.g. plain `IntegrationException`) → copied into `io.sclera.exception/`.
  - Tightly coupled to a stubbed microservice → stub class under `io.sclera.stubs.exception/` extending `RuntimeException` with the same FQN-shape so `catch` blocks compile.
- `javax.validation` annotations on copied DTOs preserved; `MethodArgumentNotValidException` continues to render 400 with field-level messages.
- **Phase 1 non-goals:** circuit breakers, retry annotations, correlation-ID propagation (deferred until stubs become real remote calls).

---

## 8. Testing

### 8.1 Test pack (Phase 1)

1. **Context-load smoke** — `ScleraCloudDeviceAssetApplicationTests` with `@SpringBootTest(webEnvironment = NONE)` against H2 via `application-test.yml`. Green = seed compiled and wired correctly. This is the most important test in the pack.
2. **Stub-contract tests** — `stubs/StubContractTests.java`. Parameterized over every class in `io.sclera.stubs/`. Asserts: no throw, safe default returned, WARN emitted (captured via `OutputCaptureExtension` or `ListAppender`).
3. **`@WebMvcTest` slice per feature family** — one positive + one negative per controller; service layer mocked with `@MockBean`. Controllers covered: DeviceController, AssetFieldController, BuildingController, DeviceSpecificationController, DeviceLifecycleHistoryController, DeviceConditionsController, ManagedSoftwareController, AssetOnboardController, ChatGPTController, NoteController, DocumentController, LocationController.
4. **`@DataJpaTest` repo smoke** — `DeviceRepositoryTest`. Saves/finds a Device against H2. If `DeviceRepository`'s native MySQL SQL doesn't parse in H2, switch to Testcontainers MySQL — fallback decision logged in `compile-fixes.md`.
5. **Manual smoke checklist** — `migration-notes/smoke-checklist.md`. Curl + `SHOW TABLES` commands run against a locally booted instance.

### 8.2 Out of test scope (Phase 1)

- Service-layer unit tests for copied logic (untested in source; not the seed-repo brief).
- End-to-end tests crossing stub boundaries (no real downstream; deferred to Phase 2).
- Load/performance tests.

---

## 9. Execution approach — Approach 3 (iterative compile-driven)

### 9.1 Phase 1 — Seed the new repo

Create `Microservice/sclera-cloud-device-asset/`. Initialize as a fresh git repo. Copy from the source repo into the new repo:
- AP-C1 controllers/services/repos/models/DTOs per §5.1.
- Shared infrastructure per §5.2: `config/`, `exception/`, `utils/` (initial pass), `enums/` (initial pass), `models/ApplicationUser*`, `dto/ApiResponse.java`.
- Adapted `pom.xml`, `application.yml`, `Dockerfile`, `mvnw`, `mvnw.cmd`.
- Write `ScleraCloudDeviceAssetApplication.java`.
- Create empty `io.sclera.stubs/` package and `migration-notes/` folder.

### 9.2 Phase 2 — Iterative compile loop

Run `mvn clean compile`. For each compile error:
- Missing class is **shared infra** → copy from source. Log as Bucket B in `compile-fixes.md`.
- Missing class belongs to **another microservice** → create stub in `io.sclera.stubs/` mirroring original signature. Log as Bucket C.
- Missing class is **edge-only** → delete the call site **from the new repo**. Log as Bucket D.
- Missing method on a class that exists → if AP-C1 needs it, copy the method body verbatim; if it touches a stubbed microservice, trim it.

Repeat until `mvn clean compile` green.

### 9.3 Phase 3 — Smoke run

`mvn spring-boot:run` against a fresh `sclera_cloud_device_asset` MySQL schema. Verify:
- Application context starts without exception.
- `SHOW TABLES;` matches the expected AP-C1 set.
- `GET /swagger-ui.html` renders only AP-C1 endpoints.
- `POST /admin/device` creates a row; `GET /admin/device/{id}` returns it.
- One AssetOnboard call produces expected WARN log lines from stubs.

### 9.4 Phase 4 — Test pack

Author and run the §8.1 test pack. `mvn test` green is the Phase-1 completion gate.

---

## 10. Out of scope (Phase 1)

- Digital twin templates implementation (only the existing `Device.digital_twin_image_url` field is retained).
- Spring Boot 3 / Java 21 / `com.sclera.*` modernization.
- Replacing stubs with real remote calls (Phase 2 — separate plan per target microservice).
- Multi-tenant isolation beyond `ApplicationUser` (CP-2 concern).
- Circuit breakers, retries, correlation-IDs.
- Performance and load testing.
- Cleanup or deletion of AP-C1 code in the source `sclera-vdms-edge-server` repo. Source repo stays read-only.

---

## 11. Migration tracking

All decisions, deferrals, and surprises land in `migration-notes/`:
- `removed-dependencies.md`
- `stubbed-services.md`
- `compile-fixes.md`
- `endpoint-diff.md`
- `known-breakages.md`
- `smoke-checklist.md`

These files are the audit trail for the extraction — a future engineer can reconstruct every choice without re-reading this design doc.

---

## 12. Acceptance criteria (Phase 1 complete)

- [ ] New repo `Microservice/sclera-cloud-device-asset/` exists as a sibling of source, initialized as a fresh git repo.
- [ ] `mvn clean compile` green.
- [ ] `mvn spring-boot:run` boots without exception against a fresh `sclera_cloud_device_asset` MySQL schema.
- [ ] Swagger UI at `/swagger-ui.html` lists only AP-C1 endpoints.
- [ ] `mvn test` green (context-load smoke + stub-contract + slice tests + repo smoke).
- [ ] All six `migration-notes/*.md` files written and reflect the actual choices made during execution.
- [ ] Source repo `sclera-vdms-edge-server` is unchanged (`git diff` against the starting commit shows no changes to its tracked files).
