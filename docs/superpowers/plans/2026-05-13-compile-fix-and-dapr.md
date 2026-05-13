# Compile Fix + VDMS Dapr Separation — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `sclera-cloud-device-asset` compile clean, then extract `vdms-service/` into a sibling repo `sclera-vdms-service/` wired to the main app via Dapr service invocation.

**Architecture:** All compilation errors are resolved by adding safe-default stub methods to existing stub services/repositories and creating two missing DTOs plus three missing model getters. The VDMS service is moved verbatim to a sibling directory; Dapr service invocation is demonstrated via one new endpoint in `DaprTestController`.

**Tech Stack:** Spring Boot 2.6.5, Java 11, Lombok, PDFBox 3.0.3, Dapr SDK 1.10.0, Docker Compose 3.8

---

## File Map

### Modified (Task 1–9)
- `src/main/java/io/sclera/service/NfcService.java`
- `src/main/java/io/sclera/service/QrCodeService.java`
- `src/main/java/io/sclera/service/ClientQrCodeService.java`
- `src/main/java/io/sclera/service/ClientNfcService.java`
- `src/main/java/io/sclera/service/GlobalQrcodeService.java`
- `src/main/java/io/sclera/service/PmsService.java`
- `src/main/java/io/sclera/service/PropertyQrcodeService.java`
- `src/main/java/io/sclera/service/APICallService.java`
- `src/main/java/io/sclera/service/MyDevicesService.java`
- `src/main/java/io/sclera/service/RecordChecklistService.java`
- `src/main/java/io/sclera/service/GlobalInspectionRecordService.java`
- `src/main/java/io/sclera/service/GlobalChecklistConditionsService.java`
- `src/main/java/io/sclera/service/IOCService.java`
- `src/main/java/io/sclera/service/DaintreeService.java`
- `src/main/java/io/sclera/sockets/SocketService.java`
- `src/main/java/io/sclera/rabbitmq/RabbitmqService.java`
- `src/main/java/io/sclera/Repository/ConnectedDevicesRepository.java` ← converted from interface to @Component class
- `src/main/java/io/sclera/Repository/MeasuringInstrumentAttributesRepository.java` ← converted from interface to @Component class
- `src/main/java/io/sclera/dto/RecordChecklistDTO.java` — add `location_id` field + getter
- `src/main/java/io/sclera/dto/DaintreeDeviceDTO.java` — add `id` field + getter
- `src/main/java/io/sclera/models/Location.java` — add 3 explicit getters
- `src/main/java/io/sclera/service/DocumentService.java` — PDFBox 3.x fix (2 lines)
- `src/main/java/io/sclera/controller/dapr/DaprTestController.java` — add `/dapr/vdms` endpoint

### Created (Task 8, 10, 11)
- `src/main/java/io/sclera/dto/DaintreeConfigurationDTO.java`
- `src/main/java/io/sclera/dto/PmsAttributesDTO.java`
- `C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\sclera-vdms-service\` (entire directory, copied from `vdms-service/`)
- `C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\docker-compose.yml`

### Deleted (Task 11)
- `vdms-service/` directory (removed from inside `sclera-cloud-device-asset`)
- `docker-compose.yml` inside `sclera-cloud-device-asset` (replaced by parent-level one)

---

## Task 1 — AP-C1edge service stubs (NfcService, QrCodeService, ClientQrCodeService, ClientNfcService, GlobalQrcodeService)

**Files:**
- Modify: `src/main/java/io/sclera/service/NfcService.java`
- Modify: `src/main/java/io/sclera/service/QrCodeService.java`
- Modify: `src/main/java/io/sclera/service/ClientQrCodeService.java`
- Modify: `src/main/java/io/sclera/service/ClientNfcService.java`
- Modify: `src/main/java/io/sclera/service/GlobalQrcodeService.java`

- [ ] **Step 1: Add missing methods to NfcService**

Replace the full file content:

```java
package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.NfcDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class NfcService {
    public JSONArray getDeviceIdsTaggedToNfc(String nfcId) { return new JSONArray(); }
    public Set<NfcDTO> getNfcsByDeviceIds(Set<String> deviceIds) { return Collections.emptySet(); }
    public Integer getQrNfcCountByDeviceId(String deviceId) { return 0; }

    public JSONArray getLocationIdsTaggedToNfc(String nfcId) {
        StubLog.warn("NfcService", "getLocationIdsTaggedToNfc", "AP-C1edge");
        return new JSONArray();
    }
    public Set<NfcDTO> getNfcsByLocationIds(Set<String> locationIds) {
        StubLog.warn("NfcService", "getNfcsByLocationIds", "AP-C1edge");
        return Collections.emptySet();
    }
}
```

- [ ] **Step 2: Add missing methods to QrCodeService**

Replace the full file content:

```java
package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.QrCodeDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class QrCodeService {
    public JSONArray getDeviceIdsTaggedToQrCode(String qrCodeId) { return new JSONArray(); }
    public Set<QrCodeDTO> getQrCodesByDeviceIds(Set<String> deviceIds) { return Collections.emptySet(); }
    public Integer getQrCodeCountByDeviceId(String deviceId) { return 0; }
    public BigInteger getMaxUpdatedQrCodeTimeStamp(String deviceId) { return null; }

    public JSONArray getLocationIdsTaggedToQrCode(String qrCodeId) {
        StubLog.warn("QrCodeService", "getLocationIdsTaggedToQrCode", "AP-C1edge");
        return new JSONArray();
    }
    public Set<QrCodeDTO> getQrCodesByLocationIds(Set<String> locationIds) {
        StubLog.warn("QrCodeService", "getQrCodesByLocationIds", "AP-C1edge");
        return Collections.emptySet();
    }
}
```

- [ ] **Step 3: Add missing methods to ClientQrCodeService**

Replace the full file content:

```java
package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.math.BigInteger;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class ClientQrCodeService {
    public JSONArray getDeviceIdsTaggedToClientQrCode(String id) { return new JSONArray(); }
    public Integer getClientQrCodeCountByDeviceId(String deviceId) { return 0; }
    public BigInteger maxUpdatedClientQrCodeTimeStamp(String deviceId) { return null; }

    public JSONArray getLocationIdsTaggedToClientQrCode(String id) {
        StubLog.warn("ClientQrCodeService", "getLocationIdsTaggedToClientQrCode", "AP-C1edge");
        return new JSONArray();
    }
}
```

- [ ] **Step 4: Add missing methods to ClientNfcService**

Replace the full file content:

```java
package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class ClientNfcService {
    public JSONArray getDeviceIdsTaggedToClientNfc(String id) { return new JSONArray(); }
    public Integer getClientNfcCountByDeviceId(String deviceId) { return 0; }

    public JSONArray getLocationIdsTaggedToClientNfc(String id) {
        StubLog.warn("ClientNfcService", "getLocationIdsTaggedToClientNfc", "AP-C1edge");
        return new JSONArray();
    }
}
```

- [ ] **Step 5: Add missing method to GlobalQrcodeService**

Replace the full file content:

```java
package io.sclera.service;

import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class GlobalQrcodeService {
    public Integer getDeviceQrcodeCountByDeviceId(String deviceId) { return 0; }

    public void deleteGlobalQRCodeByLocationId(String locationId) {
        StubLog.warn("GlobalQrcodeService", "deleteGlobalQRCodeByLocationId", "AP-C1edge");
    }
}
```

---

## Task 2 — PmsService, PropertyQrcodeService, APICallService additions

**Files:**
- Modify: `src/main/java/io/sclera/service/PmsService.java`
- Modify: `src/main/java/io/sclera/service/PropertyQrcodeService.java`
- Modify: `src/main/java/io/sclera/service/APICallService.java`

- [ ] **Step 1: Rewrite PmsService with stub methods**

Replace the full file content:

```java
package io.sclera.service;

import io.sclera.dto.PmsAttributesDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C3 */
@Service
public class PmsService {
    public List<String> getLocationIdsByRoomStatus(String vdmsId, String status) {
        StubLog.warn("PmsService", "getLocationIdsByRoomStatus", "AP-C3");
        return Collections.emptyList();
    }
    public List<PmsAttributesDTO> getPmsAttributesByLocationIds(Set<String> locationIds) {
        StubLog.warn("PmsService", "getPmsAttributesByLocationIds", "AP-C3");
        return Collections.emptyList();
    }
    public void updatePmsAttributesByLocationId(String locationId) {
        StubLog.warn("PmsService", "updatePmsAttributesByLocationId", "AP-C3");
    }
}
```

- [ ] **Step 2: Add updatePropertyServiceLocations to PropertyQrcodeService**

Replace the full file content:

```java
package io.sclera.service;

import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class PropertyQrcodeService {
    public void updatePropertyServiceLocations(String locationId) {
        StubLog.warn("PropertyQrcodeService", "updatePropertyServiceLocations", "AP-C1edge");
    }
}
```

- [ ] **Step 3: Add three missing methods to APICallService**

Append these three methods inside the `APICallService` class body, before the last closing brace. The existing methods stay unchanged. Add after the last existing method:

```java
    public Object fetchMeasuringInstruments() {
        StubLog.warn("APICallService", "fetchMeasuringInstruments", "AP-C1edge");
        return null;
    }
    public void syncLocationToADC(java.util.List<io.sclera.dto.LocationDTO> locations,
                                   String orgId, String configId, String vdmsId) {
        StubLog.warn("APICallService", "syncLocationToADC", "AP-C1edge");
    }
    public void deleteLocationFromADC(String orgId, String configId, String vdmsId,
                                       String buildingId, java.util.List<String> locationIds) {
        StubLog.warn("APICallService", "deleteLocationFromADC", "AP-C1edge");
    }
```

---

## Task 3 — MyDevicesService additions

**Files:**
- Modify: `src/main/java/io/sclera/service/MyDevicesService.java`

- [ ] **Step 1: Add missing methods to MyDevicesService**

Replace the full file content:

```java
package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.MyDevicesCompanyDTO;
import io.sclera.dto.MyDevicesSensorDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: edge-only MyDevices service - replace with remote call */
@Service
public class MyDevicesService {

    public void startMyDevicesService() {}
    public void upsertMyDevicesCompany(String username, String vdmsid, MyDevicesCompanyDTO myDevicesCompany) {}
    public void updateMyDevicesEventData(JSONObject myDevicesEventData) {}
    public List<MyDevicesCompanyDTO> getMyDevicesCompanies(String vdmsId, Integer page, Integer size) { return Collections.emptyList(); }
    public List<MyDevicesSensorDTO> getMyDevicesSensors(String vdmsId, String companyId, Integer page, Integer size) { return Collections.emptyList(); }
    public void deleteMyDevicesCompany(String id) {}
    public void deleteMyDevicesSensor(String id) {}

    public String getDeviceIdByMyDevicesSensorId(String sensorId) {
        StubLog.warn("MyDevicesService", "getDeviceIdByMyDevicesSensorId", "AP-C3");
        return null;
    }
    public Integer getMyDevicesSensorCountByDeviceId(String deviceId) {
        StubLog.warn("MyDevicesService", "getMyDevicesSensorCountByDeviceId", "AP-C3");
        return 0;
    }
    public String getMyDevicesSensorAlertStatusByDeviceId(String deviceId) {
        StubLog.warn("MyDevicesService", "getMyDevicesSensorAlertStatusByDeviceId", "AP-C3");
        return null;
    }
    public List<MyDevicesSensorDTO> getDeviceMyDevicesSensors(String vdmsId, String companyId, String deviceId) {
        StubLog.warn("MyDevicesService", "getDeviceMyDevicesSensors", "AP-C3");
        return Collections.emptyList();
    }
    public List<MyDevicesSensorDTO> getMydevicesSensorsByDeviceId(String deviceId) {
        StubLog.warn("MyDevicesService", "getMydevicesSensorsByDeviceId", "AP-C3");
        return Collections.emptyList();
    }
    public List<Object> listmydevicesDeviceAlertMessagesByDeviceIds(List<String> deviceIds) {
        StubLog.warn("MyDevicesService", "listmydevicesDeviceAlertMessagesByDeviceIds", "AP-C3");
        return Collections.emptyList();
    }
    public void updateMyDevicesSensorDeviceId(String oldDeviceId, String newDeviceId, Set<String> sensorIds) {
        StubLog.warn("MyDevicesService", "updateMyDevicesSensorDeviceId", "AP-C3");
    }
}
```

---

## Task 4 — RecordChecklist / GlobalInspection / GlobalChecklist / IOC / Daintree stubs

**Files:**
- Modify: `src/main/java/io/sclera/service/RecordChecklistService.java`
- Modify: `src/main/java/io/sclera/service/GlobalInspectionRecordService.java`
- Modify: `src/main/java/io/sclera/service/GlobalChecklistConditionsService.java`
- Modify: `src/main/java/io/sclera/service/IOCService.java`
- Modify: `src/main/java/io/sclera/service/DaintreeService.java`

- [ ] **Step 1: Add location-scoped methods to RecordChecklistService**

Replace the full file content:

```java
package io.sclera.service;

import io.sclera.dto.RecordChecklistDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C4 */
@Service
public class RecordChecklistService {

    public void updateRecordChecklistDeviceAndIsRemoved(Set<String> ids) {
        StubLog.warn("RecordChecklistService", "updateRecordChecklistDeviceAndIsRemoved", "AP-C4");
    }
    public void deleteRecordChecklistInBatch(List<String> ids) {
        StubLog.warn("RecordChecklistService", "deleteRecordChecklistInBatch", "AP-C4");
    }
    public List<String> deleteAllRecordChecklistByDeviceId(String deviceId) {
        StubLog.warn("RecordChecklistService", "deleteAllRecordChecklistByDeviceId", "AP-C4");
        return Collections.emptyList();
    }
    public void deleteAllRecordChecklistImagesByUrls(List<String> urls) {
        StubLog.warn("RecordChecklistService", "deleteAllRecordChecklistImagesByUrls", "AP-C4");
    }
    public String getRecordChecklistStatusByDeviceId(String deviceId, String x) {
        StubLog.warn("RecordChecklistService", "getRecordChecklistStatusByDeviceId", "AP-C4");
        return null;
    }
    public Integer getChecklistStatusCountDeviceId(String a, String b, String c) {
        StubLog.warn("RecordChecklistService", "getChecklistStatusCountDeviceId", "AP-C4");
        return 0;
    }
    public void updateRecordChecklistByDeviceId(String oldId, String newId, Set<String> ids) {
        StubLog.warn("RecordChecklistService", "updateRecordChecklistByDeviceId", "AP-C4");
    }
    public Set<RecordChecklistDTO> getAllRecordChecklistByBuildings(List<String> buildingIds, List<String> floorIds, List<String> locationIds) {
        StubLog.warn("RecordChecklistService", "getAllRecordChecklistByBuildings", "AP-C4");
        return new HashSet<>();
    }

    public String getRecordChecklistStatusByLocationId(String locationId, String status) {
        StubLog.warn("RecordChecklistService", "getRecordChecklistStatusByLocationId", "AP-C4");
        return null;
    }
    public Integer getChecklistStatusCountLocationId(String locationId, String a, String b) {
        StubLog.warn("RecordChecklistService", "getChecklistStatusCountLocationId", "AP-C4");
        return 0;
    }
    public void updateRecordChecklistLocationAndIsRemoved(Set<String> locationIds) {
        StubLog.warn("RecordChecklistService", "updateRecordChecklistLocationAndIsRemoved", "AP-C4");
    }
    public void deleteRecordChecklistByLocationId(String locationId) {
        StubLog.warn("RecordChecklistService", "deleteRecordChecklistByLocationId", "AP-C4");
    }
    public void deleteAllRecordChecklistByLocationId(String locationId) {
        StubLog.warn("RecordChecklistService", "deleteAllRecordChecklistByLocationId", "AP-C4");
    }
}
```

- [ ] **Step 2: Add missing method to GlobalInspectionRecordService**

Add this method inside the class body before the last `}`:

```java
    public void updateGlobalInspectionRelationLocationAndIsRemoved(Set<String> locationIds) {
        StubLog.warn("GlobalInspectionRecordService", "updateGlobalInspectionRelationLocationAndIsRemoved", "AP-C4");
    }
```

Also add import at the top of the file:
```java
import io.sclera.utils.StubLog;
```

- [ ] **Step 3: Add missing method to GlobalChecklistConditionsService**

Add this method inside the class body before the last `}`:

```java
    public void updateGlobalChecklistConditionsLocationAndIsRemoved(Set<String> locationIds) {
        StubLog.warn("GlobalChecklistConditionsService", "updateGlobalChecklistConditionsLocationAndIsRemoved", "AP-C4");
    }
```

Also add import at the top of the file:
```java
import io.sclera.utils.StubLog;
```

- [ ] **Step 4: Add missing method to IOCService**

Add this method inside the class body before the last `}`:

```java
    public void sendSensorValueDataToIOC(String deviceId, java.math.BigInteger sensorValue) {
        StubLog.warn("IOCService", "sendSensorValueDataToIOC", "edge-D");
    }
```

Also add import at the top:
```java
import io.sclera.utils.StubLog;
```

- [ ] **Step 5: Add missing method to DaintreeService**

Add this method inside the class body before the last `}`:

```java
    public java.util.List<io.sclera.dto.DaintreeConfigurationDTO> getDaintreeConfigurations(String vdmsId) {
        StubLog.warn("DaintreeService", "getDaintreeConfigurations", "AP-C2");
        return java.util.Collections.emptyList();
    }
```

Also add import at the top:
```java
import io.sclera.utils.StubLog;
```

---

## Task 5 — SocketService + RabbitmqService additions

**Files:**
- Modify: `src/main/java/io/sclera/sockets/SocketService.java`
- Modify: `src/main/java/io/sclera/rabbitmq/RabbitmqService.java`

- [ ] **Step 1: Add missing method to SocketService**

Add this method inside the class body before the last `}`:

```java
    public void socketMeasuringInstrumentSensorValueUpdate(String deviceId) {
        StubLog.warn("SocketService", "socketMeasuringInstrumentSensorValueUpdate", "edge-local");
    }
```

Also add import at the top:
```java
import io.sclera.utils.StubLog;
```

- [ ] **Step 2: Add missing method to RabbitmqService**

Replace the full file content:

```java
package io.sclera.rabbitmq;

import io.sclera.dto.touchscreen.DeviceHistoryDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.math.BigInteger;

/** STUB: edge-only AMQP publisher */
@Service
public class RabbitmqService {
    public void rabbitmqDeviceEvent(String eventType, DeviceHistoryDTO dto) {
        StubLog.warn("RabbitmqService", "rabbitmqDeviceEvent", "edge-local");
    }
    public void rabbitmqMeasuringInstrumentData(String deviceId, String sensorType,
                                                BigInteger sensorValue, String unit) {
        StubLog.warn("RabbitmqService", "rabbitmqMeasuringInstrumentData", "edge-local");
    }
}
```

---

## Task 6 — Convert ConnectedDevicesRepository to @Component stub class

**Files:**
- Modify: `src/main/java/io/sclera/Repository/ConnectedDevicesRepository.java`

- [ ] **Step 1: Check for JPA save/findById callers**

Run the following to confirm no callers use inherited JPA methods (`save`, `findById`, `deleteById`) on this repository:

```
grep -rn "connectedDevicesRepository\." src/main/java/io/sclera/ --include="*.java"
```

Expected: only calls to the 6 methods listed below. If `save` / `findById` / `deleteById` appear, add those as stubs too before proceeding.

- [ ] **Step 2: Replace file with @Component stub class**

Replace the full file content:

```java
package io.sclera.Repository;

import io.sclera.utils.StubLog;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

/** STUB: Phase 2 will implement with real JPA queries */
@Component
public class ConnectedDevicesRepository {

    public void addConnectedDevices(String deviceId, String connectedDeviceId, String type) {
        StubLog.warn("ConnectedDevicesRepository", "addConnectedDevices", "local-db");
    }
    public List<Object> getConnectedDevicesSpecifications(String deviceId, Integer page, Integer size) {
        StubLog.warn("ConnectedDevicesRepository", "getConnectedDevicesSpecifications", "local-db");
        return Collections.emptyList();
    }
    public List<Object> getConnectedSpecificationsByDeviceId(String deviceId) {
        StubLog.warn("ConnectedDevicesRepository", "getConnectedSpecificationsByDeviceId", "local-db");
        return Collections.emptyList();
    }
    public List<Object> getAllInputConnectedSpecifications(String deviceId) {
        StubLog.warn("ConnectedDevicesRepository", "getAllInputConnectedSpecifications", "local-db");
        return Collections.emptyList();
    }
    public List<Object> getAllOutputConnectedSpecifications(String deviceId) {
        StubLog.warn("ConnectedDevicesRepository", "getAllOutputConnectedSpecifications", "local-db");
        return Collections.emptyList();
    }
    public void untagPowerSource(String deviceId, String powerSourceId) {
        StubLog.warn("ConnectedDevicesRepository", "untagPowerSource", "local-db");
    }
}
```

---

## Task 7 — Convert MeasuringInstrumentAttributesRepository to @Component stub class

**Files:**
- Modify: `src/main/java/io/sclera/Repository/MeasuringInstrumentAttributesRepository.java`

- [ ] **Step 1: Check for JPA save/findById callers**

Run:

```
grep -rn "measuringInstrumentAttributesRepository\." src/main/java/io/sclera/ --include="*.java"
```

Expected: only calls to the 9 custom methods listed below. If `save` / `findById` appear, add those as stubs too.

- [ ] **Step 2: Replace file with @Component stub class**

Replace the full file content:

```java
package io.sclera.Repository;

import io.sclera.utils.StubLog;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

/** STUB: Phase 2 will implement with real JPA queries */
@Component
public class MeasuringInstrumentAttributesRepository {

    public void updateAttributeValueById(String id, String value) {
        StubLog.warn("MeasuringInstrumentAttributesRepository", "updateAttributeValueById", "local-db");
    }
    public void upsertMeasuringInstrumentAttribute(String p1, String p2, String p3, String p4,
                                                    String p5, String p6, String p7, String p8,
                                                    String p9, String p10, String p11, Integer p12) {
        StubLog.warn("MeasuringInstrumentAttributesRepository", "upsertMeasuringInstrumentAttribute", "local-db");
    }
    public Object getMeasuringInstrumentAttributeById(String id) {
        StubLog.warn("MeasuringInstrumentAttributesRepository", "getMeasuringInstrumentAttributeById", "local-db");
        return null;
    }
    public List<Object> getAllMeasuringInstrumentAttributes() {
        StubLog.warn("MeasuringInstrumentAttributesRepository", "getAllMeasuringInstrumentAttributes", "local-db");
        return Collections.emptyList();
    }
    public List<Object> getMeasuringInstrumentAttributesByMeasuringInstrumentId(String measuringInstrumentId) {
        StubLog.warn("MeasuringInstrumentAttributesRepository", "getMeasuringInstrumentAttributesByMeasuringInstrumentId", "local-db");
        return Collections.emptyList();
    }
    public void deleteMeasuringInstrumentAttributeById(String id) {
        StubLog.warn("MeasuringInstrumentAttributesRepository", "deleteMeasuringInstrumentAttributeById", "local-db");
    }
    public void deleteMeasuringInstrumentAttributeByMeasuringInstrumentId(String measuringInstrumentId) {
        StubLog.warn("MeasuringInstrumentAttributesRepository", "deleteMeasuringInstrumentAttributeByMeasuringInstrumentId", "local-db");
    }
    public void updateAttributeValue(String id, String name, String value, String unit) {
        StubLog.warn("MeasuringInstrumentAttributesRepository", "updateAttributeValue", "local-db");
    }
    public String getMeasuringInstrumentIdByIds(String p1, String p2, String p3) {
        StubLog.warn("MeasuringInstrumentAttributesRepository", "getMeasuringInstrumentIdByIds", "local-db");
        return null;
    }
}
```

---

## Task 8 — Missing DTOs + missing model/DTO getters

**Files:**
- Create: `src/main/java/io/sclera/dto/DaintreeConfigurationDTO.java`
- Create: `src/main/java/io/sclera/dto/PmsAttributesDTO.java`
- Modify: `src/main/java/io/sclera/dto/RecordChecklistDTO.java`
- Modify: `src/main/java/io/sclera/dto/DaintreeDeviceDTO.java`
- Modify: `src/main/java/io/sclera/models/Location.java`

- [ ] **Step 1: Create DaintreeConfigurationDTO**

Create new file `src/main/java/io/sclera/dto/DaintreeConfigurationDTO.java`:

```java
package io.sclera.dto;

/** STUB DTO: Phase 2 will add real fields from AP-C2 */
public class DaintreeConfigurationDTO {
}
```

- [ ] **Step 2: Create PmsAttributesDTO**

Create new file `src/main/java/io/sclera/dto/PmsAttributesDTO.java`:

```java
package io.sclera.dto;

/** STUB DTO: Phase 2 will add real fields from AP-C3 */
public class PmsAttributesDTO {
}
```

- [ ] **Step 3: Add location_id field and getter to RecordChecklistDTO**

The current file has only `building_id`, `record_type`, `inspection_record_id`. Add `location_id` field plus getter/setter:

```java
// Add inside RecordChecklistDTO class body, after the last existing field:
    private String location_id;

    public String getLocation_id() { return location_id; }
    public void setLocation_id(String v) { this.location_id = v; }
```

- [ ] **Step 4: Add id field and getter to DaintreeDeviceDTO**

The current file is an empty class. Replace the full file content:

```java
package io.sclera.dto;

/** STUB DTO: AP-C2 sensor device representation */
public class DaintreeDeviceDTO {
    private String id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
```

- [ ] **Step 5: Find and add missing getters to Location.java**

Read `src/main/java/io/sclera/models/Location.java` past line 150 and search for the fields:
- `record_checklist` (or `recordChecklist`)
- `global_inspection_relation` (or `globalInspectionRelation`)
- `global_checklist_conditions` (or `globalChecklistConditions`)

If the fields exist but getter methods are absent (possible if delombok stripped them), add explicit getters near the bottom of the class (before the last `}`). Use the actual field type found in the entity. Example form:

```java
    // Explicit getters needed by LocationService — may have been dropped by delombok
    public <FieldType> getRecord_checklist() { return this.record_checklist; }
    public <FieldType> getGlobal_inspection_relation() { return this.global_inspection_relation; }
    public <FieldType> getGlobal_checklist_conditions() { return this.global_checklist_conditions; }
```

Replace `<FieldType>` with the actual declared type of each field in the entity. If the fields do not exist at all, add both the field (`private <Type> fieldName;`) and the getter, using `Object` as the type (a safe placeholder for Phase 2).

---

## Task 9 — PDFBox 3.x fix + compile verification

**Files:**
- Modify: `src/main/java/io/sclera/service/DocumentService.java`

- [ ] **Step 1: Fix PDFBox load call**

In `DocumentService.java` at line 118, replace:

```java
pdfDocument = PDDocument.load(input);
```

with:

```java
pdfDocument = org.apache.pdfbox.Loader.getPDDocument(input);
```

Also add (or verify) this import at the top of the file:
```java
import org.apache.pdfbox.Loader;
```

Then replace:
```java
pdfDocument = PDDocument.load(input);
```

with:
```java
pdfDocument = Loader.getPDDocument(input);
```

- [ ] **Step 2: Remove canPrintDegraded() call**

At line 128, the `hasRestrictions` boolean expression includes `!permissions.canPrintDegraded()`. Remove that clause. The fixed expression is:

```java
boolean hasRestrictions =
        !permissions.canAssembleDocument() ||
                !permissions.canPrint() ||
                !permissions.canModify() ||
                !permissions.canExtractContent() ||
                !permissions.canModifyAnnotations() ||
                !permissions.canFillInForm() ||
                !permissions.canExtractForAccessibility();
```

- [ ] **Step 3: Run compile and confirm zero errors**

```
cd C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\sclera-cloud-device-asset
mvn compile
```

Expected output ends with:
```
BUILD SUCCESS
```

If errors remain, read each error message, identify which file/method is still missing, and add the corresponding stub (same pattern as Tasks 1–8) before re-running.

---

## Task 10 — Create sclera-vdms-service sibling directory

**Files:**
- Create: `C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\sclera-vdms-service\` (full copy of `vdms-service/`)

- [ ] **Step 1: Copy vdms-service to sibling directory**

Run (PowerShell):

```powershell
Copy-Item -Recurse -Force `
  "C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\sclera-cloud-device-asset\vdms-service" `
  "C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\sclera-vdms-service"
```

- [ ] **Step 2: Verify the copy**

```powershell
Get-ChildItem "C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\sclera-vdms-service" -Recurse -Name
```

Expected: shows `pom.xml`, `Dockerfile`, `src/main/java/io/sclera/vdms/VdmsController.java`, `src/main/java/io/sclera/vdms/VdmsServiceApplication.java`, `src/main/resources/application.yml`.

- [ ] **Step 3: Verify sclera-vdms-service builds on its own**

```powershell
cd "C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\sclera-vdms-service"
mvn package -DskipTests
```

Expected:
```
BUILD SUCCESS
```

---

## Task 11 — Add /dapr/vdms endpoint + parent-level docker-compose + cleanup

**Files:**
- Modify: `src/main/java/io/sclera/controller/dapr/DaprTestController.java`
- Create: `C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\docker-compose.yml`
- Delete: `vdms-service/` directory from inside `sclera-cloud-device-asset`
- Delete: `docker-compose.yml` from inside `sclera-cloud-device-asset`

- [ ] **Step 1: Add /dapr/vdms endpoint to DaprTestController**

Replace the full file content:

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

    /** Existing test endpoint — invokes vdms-service via Dapr service invocation */
    @GetMapping("/test")
    public String test() {
        byte[] result = daprClient
            .invokeMethod("vdms-service", "getVdmsInfo", null, HttpExtension.GET, byte[].class)
            .block();
        return result != null ? new String(result) : "{}";
    }

    /**
     * Dapr service invocation demo: calls GET /getVdmsInfo on sclera-vdms-service
     * via Dapr sidecar at localhost:3500.
     * Returns the VDMS status JSON from the remote service.
     */
    @GetMapping("/vdms")
    public String getVdmsStatus() {
        try {
            byte[] result = daprClient
                .invokeMethod("vdms-service", "getVdmsInfo", null, HttpExtension.GET, byte[].class)
                .block();
            return result != null ? new String(result) : "{}";
        } catch (Exception e) {
            return "{\"error\":\"vdms-service unreachable\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }
}
```

- [ ] **Step 2: Create parent-level docker-compose**

Create `C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\docker-compose.yml`:

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
    build: ./sclera-cloud-device-asset
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
      DAPR_HTTP_PORT: "3500"
    ports:
      - "8085:8085"

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
      - ./sclera-cloud-device-asset/dapr:/dapr
    network_mode: "service:app"
    depends_on:
      - app

  vdms-service:
    build: ./sclera-vdms-service
    container_name: vdms-service
    environment:
      SERVER_PORT: "8086"
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
      - ./sclera-cloud-device-asset/dapr:/dapr
    network_mode: "service:vdms-service"
    depends_on:
      - vdms-service

volumes:
  mysql_data:
```

- [ ] **Step 3: Remove vdms-service/ from inside sclera-cloud-device-asset**

```powershell
Remove-Item -Recurse -Force "C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\sclera-cloud-device-asset\vdms-service"
```

- [ ] **Step 4: Remove old docker-compose.yml from inside sclera-cloud-device-asset**

```powershell
Remove-Item -Force "C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\sclera-cloud-device-asset\docker-compose.yml"
```

- [ ] **Step 5: Verify parent compose parses correctly**

```powershell
cd "C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0"
docker compose config
```

Expected: prints the fully resolved compose YAML with no errors.

- [ ] **Step 6: Update migration-notes**

Append to `sclera-cloud-device-asset/migration-notes/status-2026-05-13.md`:

```markdown
## 2026-05-13 — Compile fix + VDMS separation

- Added missing stub methods to 14 services and 2 repositories (Tasks 1–7)
- Created DaintreeConfigurationDTO, PmsAttributesDTO (Task 8)
- Fixed Location getters, RecordChecklistDTO.location_id, DaintreeDeviceDTO.id (Task 8)
- Fixed PDFBox 3.x: Loader.getPDDocument + removed canPrintDegraded() (Task 9)
- mvn compile: BUILD SUCCESS
- vdms-service extracted to sibling sclera-vdms-service/ (Task 10)
- Parent-level docker-compose created at Sclara-2.0/docker-compose.yml (Task 11)
- /dapr/vdms endpoint added to DaprTestController (Task 11)
```
