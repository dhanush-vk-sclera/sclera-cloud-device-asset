# Design: Compile Fix + VDMS Dapr Separation

**Date:** 2026-05-13  
**Status:** Approved

---

## Overview

Two sequential tasks:

1. **Task 1 — Compile fix:** Make `sclera-cloud-device-asset` compile clean by adding missing stub methods, missing DTOs, missing model getters, and fixing a PDFBox 3.x API break.
2. **Task 2 — VDMS separation:** Extract `vdms-service/` into a sibling repo `sclera-vdms-service/`, wire both services via Dapr service invocation, and promote docker-compose to the `Sclara-2.0/` parent level.

---

## Task 1 — Compile Fix

### Scope

5 files produce compilation errors. All errors fall into four categories:

### 1a. Missing stub methods on services

Each missing method must:
- Return a safe default (see conventions below)
- Call `StubLog.warn(getClass().getSimpleName(), "<methodName>", "<target-service>")`
- Not throw

Return conventions:
- `List<T>` → `Collections.emptyList()`
- `Set<T>` → `Collections.emptySet()`
- `String` → `null`
- `Integer` / `int` → `0`
- `void` → no-op

Files and methods to add:

| Service file | Methods to add | Target service (for StubLog) |
|---|---|---|
| `MyDevicesService` | `getDeviceIdByMyDevicesSensorId(String)`, `getMyDevicesSensorCountByDeviceId(String)`, `getMyDevicesSensorAlertStatusByDeviceId(String)`, `getDeviceMyDevicesSensors(String,String,String)`, `getMydevicesSensorsByDeviceId(String)`, `listmydevicesDeviceAlertMessagesByDeviceIds(List<String>)`, `updateMyDevicesSensorDeviceId(String,String,Set<String>)` | AP-C3 |
| `RecordChecklistService` | `getRecordChecklistStatusByLocationId(String,String)`, `getChecklistStatusCountLocationId(String,String,String)`, `updateRecordChecklistLocationAndIsRemoved(Set<String>)`, `deleteRecordChecklistByLocationId(String)`, `deleteAllRecordChecklistByLocationId(String)` | AP-C4 |
| `GlobalInspectionRecordService` | `updateGlobalInspectionRelationLocationAndIsRemoved(Set<String>)` | AP-C4 |
| `GlobalChecklistConditionsService` | `updateGlobalChecklistConditionsLocationAndIsRemoved(Set<String>)` | AP-C4 |
| `PropertyQrcodeService` | `updatePropertyServiceLocations(String)` | AP-C1edge |
| `GlobalQrcodeService` | `deleteGlobalQRCodeByLocationId(String)` | AP-C1edge |
| `PmsService` | `getLocationIdsByRoomStatus(String,String)`, `getPmsAttributesByLocationIds(Set<String>)`, `updatePmsAttributesByLocationId(String)` | AP-C3 |
| `NfcService` | `getLocationIdsTaggedToNfc(String)`, `getNfcsByLocationIds(Set<String>)` | AP-C1edge |
| `QrCodeService` | `getLocationIdsTaggedToQrCode(String)`, `getQrCodesByLocationIds(Set<String>)` | AP-C1edge |
| `ClientQrCodeService` | `getLocationIdsTaggedToClientQrCode(String)` | AP-C1edge |
| `ClientNfcService` | `getLocationIdsTaggedToClientNfc(String)` | AP-C1edge |
| `IOCService` | `sendSensorValueDataToIOC(String,BigInteger)` | edge-D |
| `DaintreeService` | `getDaintreeConfigurations(String)` | AP-C2 |
| `APICallService` | `fetchMeasuringInstruments()`, `syncLocationToADC(List<LocationDTO>,String,String,String)`, `deleteLocationFromADC(String,String,String,String,List<String>)` | AP-C1edge |
| `SocketService` | `socketMeasuringInstrumentSensorValueUpdate(String)` | edge-local |
| `RabbitmqService` | `rabbitmqMeasuringInstrumentData(String,String,BigInteger,String)` | edge-local |

### 1b. Missing stub methods on repositories

Same stub contract — return safe defaults, call `StubLog.warn`.

| Repository file | Methods to add |
|---|---|
| `ConnectedDevicesRepository` | `addConnectedDevices(String,String,String)`, `getConnectedDevicesSpecifications(String,Integer,Integer)`, `getConnectedSpecificationsByDeviceId(String)`, `getAllInputConnectedSpecifications(String)`, `getAllOutputConnectedSpecifications(String)`, `untagPowerSource(String,String)` |
| `MeasuringInstrumentAttributesRepository` | `updateAttributeValueById(String,String)`, `upsertMeasuringInstrumentAttribute(String,String,String,String,String,String,String,String,String,String,String,Integer)`, `getMeasuringInstrumentAttributeById(String)`, `getAllMeasuringInstrumentAttributes()`, `getMeasuringInstrumentAttributesByMeasuringInstrumentId(String)`, `deleteMeasuringInstrumentAttributeById(String)`, `deleteMeasuringInstrumentAttributeByMeasuringInstrumentId(String)`, `updateAttributeValue(String,String,String,String)`, `getMeasuringInstrumentIdByIds(String,String,String)` |

Note: these repositories are currently JPA interfaces (`extends JpaRepository<...>`). Interfaces cannot hold method bodies. Both must be refactored: remove the `extends JpaRepository` declaration and the `@Repository` annotation, replace with `@Component`, and implement each method as a stub body. Any existing callers that relied on JPA-derived `save`/`findById`/etc. must also be stubbed. Verify there are no such usages before changing.

### 1c. Missing DTOs

Create two minimal stub DTOs in `src/main/java/io/sclera/dto/`:

- `DaintreeConfigurationDTO` — empty POJO, Lombok `@Data` + `@NoArgsConstructor`
- `PmsAttributesDTO` — empty POJO, Lombok `@Data` + `@NoArgsConstructor`

### 1d. Missing model / DTO getters

- `Location.java` — check fields `record_checklist`, `global_inspection_relation`, `global_checklist_conditions`. If they exist but getters were dropped during delombok, add explicit `public <Type> getRecord_checklist()`, etc.
- `RecordChecklistDTO.java` — check field `location_id`; add `getLocation_id()` if absent.
- `DaintreeDeviceDTO.java` — check field `id`; add `getId()` if absent.

### 1e. PDFBox 3.x fix — `DocumentService.java`

- Line 118: replace `PDDocument.load(input)` with `Loader.getPDDocument(input)` and add import `org.apache.pdfbox.Loader`.
- Line 128: remove `!permissions.canPrintDegraded()` from the `hasRestrictions` boolean expression. The method was removed in PDFBox 3; `canPrint()` already covers degraded printing intent.

### Success criterion

`mvn compile` exits 0 with no errors.

---

## Task 2 — VDMS Separation + Dapr

### Directory layout after change

```
Sclara-2.0/
├── sclera-cloud-device-asset/     (unchanged except nested vdms-service/ removed)
│   └── dapr/                      (config + components, volume-mounted by both sidecars)
├── sclera-vdms-service/           (NEW sibling — verbatim copy of vdms-service/)
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/io/sclera/vdms/
└── docker-compose.yml             (NEW at Sclara-2.0 level)
```

### What moves

Copy `sclera-cloud-device-asset/vdms-service/` → `sclera-vdms-service/` with no code changes. Delete `vdms-service/` from inside `sclera-cloud-device-asset`. Remove the now-redundant `docker-compose.yml` inside `sclera-cloud-device-asset`.

### Dapr service invocation wiring

**`sclera-vdms-service` (no code changes needed):**  
`VdmsController.GET /vdms` already exists. The Dapr sidecar auto-routes inbound invocation calls to the app.

**`sclera-cloud-device-asset` — two additions:**

1. `VdmsDaprClient.java` (new class in `io.sclera.service` or `io.sclera.controller.dapr`):
   - `@Component` Spring bean
   - Injects `RestTemplate` or uses `java.net.http.HttpClient`
   - One method: `getVdmsStatus()` — calls `GET http://localhost:${DAPR_HTTP_PORT:3500}/v1.0/invoke/vdms-service/method/vdms`
   - Returns the raw JSON string or a typed DTO
   - Logs WARN on any exception and returns a fallback empty response

2. `DaprTestController.java` (existing file) — add one endpoint:
   - `GET /dapr/vdms` — calls `VdmsDaprClient.getVdmsStatus()` and returns the result
   - Demonstrates end-to-end Dapr service invocation

### docker-compose at Sclara-2.0 level

Five services:

| Service | Image / Build | Port | Dapr app-id |
|---|---|---|---|
| `mysql` | `mysql:8` | 3306 | — |
| `app` | `./sclera-cloud-device-asset` | 8085 | `sclera-cloud-device-asset` |
| `app-dapr` | `daprio/daprd:1.12.0` | 3500/50001 | sidecar for app |
| `vdms-service` | `./sclera-vdms-service` | 8086 | `vdms-service` |
| `vdms-dapr` | `daprio/daprd:1.12.0` | 3501/50002 | sidecar for vdms-service |

Both Dapr sidecars mount `./sclera-cloud-device-asset/dapr` for config and components.

### Success criterion

`docker compose up` at `Sclara-2.0/` starts all 5 services.  
`curl http://localhost:8085/dapr/vdms` returns the VDMS status JSON from `sclera-vdms-service`.

---

## What is NOT in scope

- Real business logic in any stub (Phase 2)
- Database schema changes
- Any changes to `sclera-vdms-service` beyond moving it
- Pub/sub Dapr wiring
- Tests
