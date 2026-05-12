# sclera-cloud-device-asset Extraction — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stand up a new sibling repo `Microservice/sclera-cloud-device-asset` by verbatim-copying the AP-C1 feature slice (15 features per `docs/superpowers/specs/2026-05-12-sclera-cloud-device-asset-design.md` §2) out of the read-only source `sclera-vdms-edge-server`, stubbing every cross-microservice dependency, and proving the result compiles, boots, and passes a small test pack.

**Architecture:** Approach 3 — iterative compile-driven extraction. Phase 1 seeds the new repo with the AP-C1 whitelist + shared infra. Phase 2 runs `mvn compile` in a loop, classifying each missing class as Bucket B (shared, copy), Bucket C (other microservice, stub), or Bucket D (edge-only, delete call site). Phase 3 boots the app against a fresh MySQL schema. Phase 4 writes a focused test pack and runs `mvn test`.

**Tech Stack:** Spring Boot 2.6.5, Java 11, Maven, MySQL 8, H2 (test), Spring Data JPA, Spring Security OAuth2 Resource Server + JWT, springdoc-openapi, Lombok, Caffeine, Jackson. Package root `io.sclera.*` (verbatim — not the `com.sclera.<app>` of greenfield services).

**Spec reference:** `docs/superpowers/specs/2026-05-12-sclera-cloud-device-asset-design.md`. Read it first.

**Source repo invariant:** `sclera-vdms-edge-server` is **read-only** for the duration of this plan. No task in this plan modifies a file in the source repo. All operations are additive in the new repo `Microservice/sclera-cloud-device-asset`.

**TDD note:** Classical TDD does not apply to Phase 1 (verbatim copy of existing untested code). Tests live in Phase 4. Each Phase 1–3 task ends with `git commit` so progress is recoverable.

---

## Pre-flight checks

Before starting Task 1, confirm:

- [ ] Working directory is `C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice\sclera-vdms-edge-server` (the source repo).
- [ ] The path `C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice\sclera-cloud-device-asset` does **not** yet exist.
- [ ] `mvn -v` works and reports Java 11 (or Java 11-compatible).
- [ ] `mysql --version` works; you can connect to a local MySQL 8 with credentials that match the values you will put in `application.yml` at Task 3.
- [ ] `git --version` works.
- [ ] The spec file `docs/superpowers/specs/2026-05-12-sclera-cloud-device-asset-design.md` is open in your editor for reference.

---

## Task 1: Create new repo skeleton

**Files:**
- Create: `Microservice/sclera-cloud-device-asset/` (root)
- Create: `Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/{config,controller/admin,dto,enums,exception,models,Repository,service,stubs,utils}/`
- Create: `Microservice/sclera-cloud-device-asset/src/main/resources/External_jar/`
- Create: `Microservice/sclera-cloud-device-asset/src/test/java/io/sclera/{controller/admin,stubs,Repository}/`
- Create: `Microservice/sclera-cloud-device-asset/migration-notes/`

- [ ] **Step 1: Create the new repo root and full directory tree.**

Run from the source repo working directory:

```powershell
$dest = "..\sclera-cloud-device-asset"
$dirs = @(
  "src\main\java\io\sclera\config",
  "src\main\java\io\sclera\controller\admin",
  "src\main\java\io\sclera\dto",
  "src\main\java\io\sclera\enums",
  "src\main\java\io\sclera\exception",
  "src\main\java\io\sclera\models",
  "src\main\java\io\sclera\Repository",
  "src\main\java\io\sclera\service",
  "src\main\java\io\sclera\stubs",
  "src\main\java\io\sclera\utils",
  "src\main\resources\External_jar",
  "src\test\java\io\sclera\controller\admin",
  "src\test\java\io\sclera\stubs",
  "src\test\java\io\sclera\Repository",
  "migration-notes"
)
foreach ($d in $dirs) { New-Item -ItemType Directory -Force -Path (Join-Path $dest $d) | Out-Null }
Get-ChildItem -Recurse -Directory $dest | Select-Object FullName
```

Expected: 16 directories printed under the new repo root.

- [ ] **Step 2: Copy the Maven wrapper from source.**

```powershell
$src = (Get-Location).Path
$dest = "..\sclera-cloud-device-asset"
Copy-Item -Path "$src\mvnw","$src\mvnw.cmd" -Destination $dest
Copy-Item -Path "$src\.mvn" -Destination $dest -Recurse -ErrorAction SilentlyContinue
Get-ChildItem $dest | Select-Object Name
```

Expected output includes `mvnw`, `mvnw.cmd`, and (if it exists in source) the `.mvn` directory.

- [ ] **Step 3: Verify the source `.mvn` exists; if it does not, skip silently — the parent `spring-boot-starter-parent` resolves Maven Wrapper from the binary `mvnw.cmd` alone for our purposes.**

```powershell
Test-Path "..\sclera-cloud-device-asset\.mvn"
```

Either result is acceptable. Log in `migration-notes/compile-fixes.md` if `False`.

---

## Task 2: Create the new pom.xml

**Files:**
- Create: `Microservice/sclera-cloud-device-asset/pom.xml`

The new `pom.xml` is the source `pom.xml` with these changes:
- artifactId `Sclera` → `sclera-cloud-device-asset`
- name `Sclera` → `sclera-cloud-device-asset`
- description → "AP-C1 cloud device/asset microservice (extracted from sclera-vdms-edge-server)"
- finalName `sclera` → `sclera-cloud-device-asset`
- Remove: `spring-boot-starter-amqp`, `spring-boot-starter-websocket`, `spring-boot-starter-thymeleaf`, `javax.websocket-api`, `webjars-locator`, `sockjs-client`, `stomp-websocket`, `bootstrap`, `jquery`, `commons-net`, `twilio`
- Remove system jar entries: `lorawan`, `modbus`, `mri`, `Haystack`
- Keep system jar entry: `ScleraTools` (will be revisited in Phase 2 if not actually linked)
- Add: `h2` (test scope) for `@DataJpaTest` and the context-load smoke

- [ ] **Step 1: Write the new `pom.xml`** (use the full XML shown below — note `groupId` is `io.sclera`):

The XML body is too long to inline here without escaping issues — copy it from `docs/superpowers/plans/2026-05-12-sclera-cloud-device-asset-pom-template.xml` (created alongside this plan) into `..\sclera-cloud-device-asset\pom.xml`, then `mvn -v` in the new repo to confirm Maven picks it up.

If the template file is not present in this repo, recreate the file from the source `pom.xml` by applying the deltas listed above. Use `Set-Content -Encoding utf8`.

- [ ] **Step 2: Sanity-check the XML parses.**

```powershell
[xml]$x = Get-Content "..\sclera-cloud-device-asset\pom.xml"
$x.project.artifactId
$x.project.dependencies.dependency.Count
```

Expected: `sclera-cloud-device-asset` and a count around 35.

---

## Task 3: Create application.yml and application-test.yml

**Files:**
- Create: `Microservice/sclera-cloud-device-asset/src/main/resources/application.yml`
- Create: `Microservice/sclera-cloud-device-asset/src/main/resources/application-test.yml`

- [ ] **Step 1: Create `application.yml`** with these key settings:

- `server.port`: 8085
- `spring.application.name`: sclera-cloud-device-asset
- `spring.datasource.url`: `jdbc:mysql://localhost:3306/sclera_cloud_device_asset?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true`
- `spring.datasource.username`/`password`: `${DB_USER:root}` / `${DB_PASS:root}`
- `spring.jpa.hibernate.ddl-auto`: `update`
- `spring.jpa.properties.hibernate.dialect`: `org.hibernate.dialect.MySQL8Dialect`
- `spring.security.oauth2.resourceserver.jwt.issuer-uri`: `${KEYCLOAK_ISSUER_URI:http://localhost:8080/auth/realms/sclera}`
- `springdoc.swagger-ui.path`: `/swagger-ui.html`
- `logging.level.io.sclera.stubs`: `WARN`

- [ ] **Step 2: Create `application-test.yml`** (used by `@SpringBootTest` and `@DataJpaTest`):

- `spring.datasource.url`: `jdbc:h2:mem:scleratest;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1`
- `spring.datasource.driver-class-name`: `org.h2.Driver`
- `spring.jpa.hibernate.ddl-auto`: `create-drop`
- `spring.jpa.properties.hibernate.dialect`: `org.hibernate.dialect.H2Dialect`
- `spring.security.oauth2.resourceserver.jwt.issuer-uri`: `http://localhost/test-issuer`

Write each with `Set-Content -Path ... -Value $yml -Encoding utf8`.

---

## Task 4: Create Dockerfile and docker-compose.yml

**Files:**
- Create: `Microservice/sclera-cloud-device-asset/Dockerfile`
- Create: `Microservice/sclera-cloud-device-asset/docker-compose.yml`

- [ ] **Step 1: Write `Dockerfile`** with these contents (use `Set-Content -Encoding utf8`):

```dockerfile
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY target/sclera-cloud-device-asset.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

- [ ] **Step 2: Write `docker-compose.yml`** with one `mysql:8` service exposing 3306 + creating database `sclera_cloud_device_asset`, and one `app` service built from `.` exposing 8085, with env `DB_USER=root`, `DB_PASS=root`, `KEYCLOAK_ISSUER_URI` (the value matches the local Keycloak issuer the monolith uses).

---

## Task 5: Create README.md, CLAUDE.md, .gitignore, migration-notes templates

**Files:**
- Create: `Microservice/sclera-cloud-device-asset/{README.md,CLAUDE.md,.gitignore}`
- Create: `Microservice/sclera-cloud-device-asset/migration-notes/{removed-dependencies,stubbed-services,compile-fixes,endpoint-diff,known-breakages,smoke-checklist}.md`

- [ ] **Step 1: Write `README.md`** — title, status, stack, run instructions, links to the design and plan in this source repo.

- [ ] **Step 2: Write `CLAUDE.md`** — declares the new repo's invariants (verbatim Phase 1; `io.sclera.*` root; do not modernize without a Phase-2 plan; stubs never throw; source repo is read-only).

- [ ] **Step 3: Write `.gitignore`** — at minimum `target/`, `.idea/`, `.vscode/`, `*.iml`, `*.log`.

- [ ] **Step 4: Write six `migration-notes/*.md`** templates with appropriate headers. Each has a brief intro paragraph followed by an empty table or checklist for the engineer to fill in during execution:
  - `removed-dependencies.md` — `| Coordinate | Reason | Source line |`
  - `stubbed-services.md` — `| Stub class | Target microservice | Methods mirrored | Notes |`
  - `compile-fixes.md` — `| Date | Missing symbol | Classification (B/C/D) | Action | Notes |`
  - `endpoint-diff.md` — sections for Kept / Dropped / Relocated
  - `known-breakages.md` — paragraph followed by an empty list
  - `smoke-checklist.md` — checklist items from spec §9.3

---

## Task 6: Initial git commit (pre-source-copy snapshot)

- [ ] **Step 1: `git init && git branch -M main`** in `..\sclera-cloud-device-asset`.
- [ ] **Step 2: `git config user.email backend.amp@sclera.com` and `user.name dhanush-vk-sclera`** if global config does not already match.
- [ ] **Step 3: `git add . && git commit -m "chore: bootstrap sclera-cloud-device-asset skeleton"`**

Expected: one commit on `main`, working tree clean.

---

## Task 7: Write the Spring application entry point

**Files:**
- Create: `Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/ScleraCloudDeviceAssetApplication.java`

- [ ] **Step 1: Write the application class.**

```java
package io.sclera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScleraCloudDeviceAssetApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScleraCloudDeviceAssetApplication.class, args);
    }
}
```

Use `Set-Content -Encoding utf8` to write it.

- [ ] **Step 2: Commit.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/main/java/io/sclera/ScleraCloudDeviceAssetApplication.java
git commit -m "feat: add ScleraCloudDeviceAssetApplication entry point"
Pop-Location
```

---

## Task 8: Copy AP-C1 controllers

**Files (copy from source `src/main/java/io/sclera/controller/admin/` to new repo same path):**

Whitelist (20 files): AiCallLogController, AssetFieldController, AssetOnboardController, BuildingController, ChatGPTController, DeviceConditionsController, DeviceController, DeviceLifecycleHistoryController, DeviceSpecificationController, DeviceTechnicianAISuggestionController, DocumentController, FloorController, LocationController, LocationHistoryController, ManagedSoftwareController, MediaController, MyDevicesController, NoteController, PropertyServiceController, SpecificationsController.

- [ ] **Step 1: Copy the listed controllers.**

```powershell
$srcDir = "src\main\java\io\sclera\controller\admin"
$destDir = "..\sclera-cloud-device-asset\$srcDir"
$controllers = @(
  "AiCallLogController","AssetFieldController","AssetOnboardController","BuildingController",
  "ChatGPTController","DeviceConditionsController","DeviceController","DeviceLifecycleHistoryController",
  "DeviceSpecificationController","DeviceTechnicianAISuggestionController","DocumentController","FloorController",
  "LocationController","LocationHistoryController","ManagedSoftwareController","MediaController",
  "MyDevicesController","NoteController","PropertyServiceController","SpecificationsController"
)
foreach ($c in $controllers) {
  Copy-Item -Path "$srcDir\$c.java" -Destination $destDir -ErrorAction Stop
}
(Get-ChildItem $destDir -Filter "*.java").Count
```

Expected: `20`.

- [ ] **Step 2: Commit.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/main/java/io/sclera/controller/admin/
git commit -m "feat: copy AP-C1 controllers verbatim from sclera-vdms-edge-server"
Pop-Location
```

---

## Task 9: Copy AP-C1 services

**Files (copy from `src/main/java/io/sclera/service/`):**

Whitelist (23 files): AddressService, AiCallService, AssetOnboardService, BuildingService, ChatGPTService, DeviceConditionsService, DeviceInstalledAppsService, DeviceLifecycleHistoryService, DeviceOnboardStatusAssigneeService, DeviceSearchService, DeviceService, DeviceSpecificationService, DeviceTechnicianAISuggestionService, DeviceTypeService, DocumentService, FloorService, LocationHistoryService, LocationService, ManagedSoftwareSearchService, ManagedSoftwareService, MediaService, MyDevicesService, NotesService.

- [ ] **Step 1: Copy the listed services.**

```powershell
$srcDir = "src\main\java\io\sclera\service"
$destDir = "..\sclera-cloud-device-asset\$srcDir"
$services = @(
  "AddressService","AiCallService","AssetOnboardService","BuildingService","ChatGPTService",
  "DeviceConditionsService","DeviceInstalledAppsService","DeviceLifecycleHistoryService","DeviceOnboardStatusAssigneeService",
  "DeviceSearchService","DeviceService","DeviceSpecificationService","DeviceTechnicianAISuggestionService","DeviceTypeService",
  "DocumentService","FloorService","LocationHistoryService","LocationService","ManagedSoftwareSearchService",
  "ManagedSoftwareService","MediaService","MyDevicesService","NotesService"
)
foreach ($s in $services) {
  Copy-Item -Path "$srcDir\$s.java" -Destination $destDir -ErrorAction Stop
}
(Get-ChildItem $destDir -Filter "*.java").Count
```

Expected: `23`.

- [ ] **Step 2: Commit.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/main/java/io/sclera/service/
git commit -m "feat: copy AP-C1 services verbatim"
Pop-Location
```

---

## Task 10: Copy AP-C1 repositories

**Files (copy from `src/main/java/io/sclera/Repository/`):**

Whitelist (25 files): AddressRepository, AiCallLogHistoryRepository, AiCallLogRepository, ApplicationUserRepository, AssetDeviceMappingRepository, AssetFieldRepository, AssetRepository, BuildingRepository, DeviceConditionsRepository, DeviceInstalledAppsRepository, DeviceIPAddressRepository, DeviceLifeCycleHistoryRepository, DeviceNetworkSpecificationRepository, DeviceOnboardStatusAssigneeRepository, DeviceOnboardStatusRepository, DeviceRepository, DeviceSpecificationRepository, DeviceTechnicianAISuggestionRepository, DeviceTypesRepository, DocumentRepository, FloorRepository, LocationHistoryRepository, LocationRepository, ManagedSoftwareRepository, MediaRepository.

- [ ] **Step 1: Copy the listed repositories.**

```powershell
$srcDir = "src\main\java\io\sclera\Repository"
$destDir = "..\sclera-cloud-device-asset\$srcDir"
$repos = @(
  "AddressRepository","AiCallLogHistoryRepository","AiCallLogRepository","ApplicationUserRepository",
  "AssetDeviceMappingRepository","AssetFieldRepository","AssetRepository","BuildingRepository",
  "DeviceConditionsRepository","DeviceInstalledAppsRepository","DeviceIPAddressRepository","DeviceLifeCycleHistoryRepository",
  "DeviceNetworkSpecificationRepository","DeviceOnboardStatusAssigneeRepository","DeviceOnboardStatusRepository",
  "DeviceRepository","DeviceSpecificationRepository","DeviceTechnicianAISuggestionRepository","DeviceTypesRepository",
  "DocumentRepository","FloorRepository","LocationHistoryRepository","LocationRepository",
  "ManagedSoftwareRepository","MediaRepository"
)
foreach ($r in $repos) {
  Copy-Item -Path "$srcDir\$r.java" -Destination $destDir -ErrorAction Stop
}
(Get-ChildItem $destDir -Filter "*.java").Count
```

Expected: `25`.

- [ ] **Step 2: Commit.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/main/java/io/sclera/Repository/
git commit -m "feat: copy AP-C1 repositories verbatim (capital R preserved)"
Pop-Location
```

---

## Task 11: Copy AP-C1 entity models

**Files (copy from `src/main/java/io/sclera/models/`):**

Whitelist (24 files): Address, AiCallLog, AiCallLogHistory, ApplicationUser, Asset, AssetDeviceMapping, AssetField, Building, Device, DeviceConditions, DeviceInstalledApps, DeviceLifecycleHistory, DeviceNetworkSpecification, DeviceOnboardStatus, DeviceOnboardStatusAssignee, DeviceSpecification, DeviceTechnicianAISuggestion, DeviceTypes, Device_IP_Address, Document, Floor, Location, LocationHistory, ManagedSoftware.

- [ ] **Step 1: Copy the listed models.**

```powershell
$srcDir = "src\main\java\io\sclera\models"
$destDir = "..\sclera-cloud-device-asset\$srcDir"
$models = @(
  "Address","AiCallLog","AiCallLogHistory","ApplicationUser","Asset","AssetDeviceMapping","AssetField",
  "Building","Device","DeviceConditions","DeviceInstalledApps","DeviceLifecycleHistory",
  "DeviceNetworkSpecification","DeviceOnboardStatus","DeviceOnboardStatusAssignee","DeviceSpecification",
  "DeviceTechnicianAISuggestion","DeviceTypes","Device_IP_Address",
  "Document","Floor","Location","LocationHistory","ManagedSoftware"
)
foreach ($m in $models) {
  Copy-Item -Path "$srcDir\$m.java" -Destination $destDir -ErrorAction Stop
}
(Get-ChildItem $destDir -Filter "*.java").Count
```

Expected: `24`.

- [ ] **Step 2: Commit.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/main/java/io/sclera/models/
git commit -m "feat: copy AP-C1 entity models verbatim"
Pop-Location
```

---

## Task 12: Copy AP-C1 DTOs

**Files (copy from `src/main/java/io/sclera/dto/`):**

Whitelist (initial set — compile pass adds more if needed): AiCallLogDTO, AiCallLogHistoryDTO, AssetFieldDTO, BuildingDTO, ChatGPTDTO, DeviceAlertDTO, DeviceConditionsDTO, DeviceDetailDTO, DeviceDTO, DeviceInstalledAppsDTO, DeviceLifecycleHistoryDTO, DeviceOnboardStatusAssigneeDTO, DeviceOnboardStatusDTO, DeviceSpecificationDTO, DeviceTechnicianAISuggestionDTO, DevicedataDTO, DevicesDTO, DeviceTopologyDTO, DeviceTypesDTO, DocumentMediaDTO, FloorDTO. Plus `ApiResponse.java` if it exists in `dto/`.

- [ ] **Step 1: Copy the listed DTOs.**

```powershell
$srcDir = "src\main\java\io\sclera\dto"
$destDir = "..\sclera-cloud-device-asset\$srcDir"
$dtos = @(
  "AiCallLogDTO","AiCallLogHistoryDTO","AssetFieldDTO","BuildingDTO","ChatGPTDTO",
  "DeviceAlertDTO","DeviceConditionsDTO","DeviceDetailDTO","DeviceDTO","DeviceInstalledAppsDTO",
  "DeviceLifecycleHistoryDTO","DeviceOnboardStatusAssigneeDTO","DeviceOnboardStatusDTO","DeviceSpecificationDTO",
  "DeviceTechnicianAISuggestionDTO","DevicedataDTO","DevicesDTO","DeviceTopologyDTO","DeviceTypesDTO",
  "DocumentMediaDTO","FloorDTO","ApiResponse"
)
foreach ($d in $dtos) {
  Copy-Item -Path "$srcDir\$d.java" -Destination $destDir -ErrorAction SilentlyContinue
}
(Get-ChildItem $destDir -Filter "*.java").Count
```

Expected: most or all 22 files copied. Any missed file gets surfaced by the compile pass.

- [ ] **Step 2: Commit.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/main/java/io/sclera/dto/
git commit -m "feat: copy AP-C1 DTOs (initial pass; compile loop will add more)"
Pop-Location
```

---

## Task 13: Copy shared infrastructure (Bucket B) — config, exception, enums, utils

- [ ] **Step 1: Copy `config/`, `exception/`, `enums/`, `utils/` wholesale.**

```powershell
$pkgs = @("config","exception","enums","utils")
foreach ($p in $pkgs) {
  $src = "src\main\java\io\sclera\$p"
  $dest = "..\sclera-cloud-device-asset\src\main\java\io\sclera\$p"
  if (Test-Path $src) {
    Copy-Item -Path "$src\*" -Destination $dest -Recurse -Force
  }
}
foreach ($p in $pkgs) {
  $count = (Get-ChildItem "..\sclera-cloud-device-asset\src\main\java\io\sclera\$p" -Filter "*.java" -Recurse).Count
  Write-Output "$p`: $count files"
}
```

Expected: non-zero counts for each. Record counts in `migration-notes/compile-fixes.md`.

- [ ] **Step 2: Copy `ScleraTools.jar` if present in source.**

```powershell
$jar = "src\main\resources\External_jar\ScleraTools.jar"
if (Test-Path $jar) {
  Copy-Item -Path $jar -Destination "..\sclera-cloud-device-asset\src\main\resources\External_jar\"
}
```

- [ ] **Step 3: Commit.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/main/java/io/sclera/config/ src/main/java/io/sclera/exception/ src/main/java/io/sclera/enums/ src/main/java/io/sclera/utils/ src/main/resources/External_jar/
git commit -m "feat: copy shared infrastructure (config, exception, enums, utils, ScleraTools.jar)"
Pop-Location
```

---

## Task 14: First compile attempt + record the error landscape

- [ ] **Step 1: Run `mvn clean compile`.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
mvn -B -q clean compile 2>&1 | Tee-Object -FilePath ..\sclera-vdms-edge-server\target-tmp-compile-pass-1.log
Pop-Location
```

Expected: **compile FAILURE**. This is by design.

- [ ] **Step 2: Extract the unique missing symbols.**

```powershell
Select-String -Path "target-tmp-compile-pass-1.log" -Pattern "cannot find symbol|package .* does not exist" | ForEach-Object { $_.Line.Trim() } | Sort-Object -Unique | Set-Content "target-tmp-missing-symbols-pass-1.txt"
(Get-Content "target-tmp-missing-symbols-pass-1.txt").Count
```

Expected: a count between 20 and 200.

- [ ] **Step 3: Append the count and a 20-line sample to `migration-notes/compile-fixes.md`.**

```powershell
$sample = Get-Content "target-tmp-missing-symbols-pass-1.txt" -TotalCount 20 -ErrorAction SilentlyContinue
$body = "`n## Pass 1 - initial compile`n`nUnique missing-symbol error lines: $((Get-Content 'target-tmp-missing-symbols-pass-1.txt').Count)`n`n### Sample (first 20)`n`n``````n$($sample -join "`n")`n``````n"
Add-Content -Path "..\sclera-cloud-device-asset\migration-notes\compile-fixes.md" -Value $body -Encoding utf8
```

- [ ] **Step 4: Commit progress.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add migration-notes/compile-fixes.md
git commit -m "chore: record initial compile pass error landscape"
Pop-Location
```

---

## Task 15: Compile loop procedure (Phase 2 - iterative)

**Procedure - repeat until `mvn clean compile` is green.** Each iteration is its own commit.

### 15a. Stub template

When the compile loop says a class is missing and that class belongs to another microservice (per spec §5.3), create a stub. Template:

```java
package io.sclera.stubs;

import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

/**
 * TODO: replace with remote call to AP-C5 sclera-cloud-alert/notification
 */
@Service
public class AlertProfileService {

    public Object attachDefault(Long deviceId) {
        StubLog.warn(AlertProfileService.class, "attachDefault", "AP-C5", deviceId);
        return null;
    }

    public java.util.List<Object> findByDevice(Long deviceId) {
        StubLog.warn(AlertProfileService.class, "findByDevice", "AP-C5", deviceId);
        return java.util.Collections.emptyList();
    }
}
```

Exact method names and return types must mirror what the calling AP-C1 code references. Read the compile error to know which method and what return type, then write a stub method with the matching signature.

### 15b. `StubLog` helper (write once, used by every stub)

- [ ] **Step 1: Create `io.sclera.utils.StubLog`.**

```java
package io.sclera.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.Objects;

public final class StubLog {
    private static final Logger log = LoggerFactory.getLogger("io.sclera.stubs");

    private StubLog() {}

    public static void warn(Class<?> stubClass, String method, String targetMicroservice, Object... args) {
        log.warn("STUB CALL: class={} method={} target_microservice={} args_hash={}",
                stubClass.getName(), method, targetMicroservice, Objects.hash(Arrays.deepHashCode(args)));
    }
}
```

Write with `Set-Content -Encoding utf8` to `..\sclera-cloud-device-asset\src\main\java\io\sclera\utils\StubLog.java`.

- [ ] **Step 2: Commit `StubLog`.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/main/java/io/sclera/utils/StubLog.java
git commit -m "feat(utils): add StubLog helper for io.sclera.stubs/"
Pop-Location
```

### 15c. Classification decision for each missing symbol

| Pattern | Bucket | Action |
|---|---|---|
| `io.sclera.utils.*`, `io.sclera.config.*`, `io.sclera.exception.*`, `io.sclera.enums.*`, `io.sclera.models.ApplicationUser*`, `io.sclera.dto.ApiResponse*` | B | Copy missing file from source (Step 15d) |
| `io.sclera.dto.*` for an AP-C1 feature (`DeviceXxxDTO`, `BuildingXxxDTO`, etc.) | B | Copy from source |
| `AlertProfile*`, `Alert*`, `AlertDowntimeSchedule*` | C | Stub per AP-C5 |
| `Integration*`, `Bacnet*`, `Snmp*`, `Daintree*`, `Disruptive*`, `Awair*`, `Ecobee*`, `Monnit*`, `Lorawan*`, `Modbus*`, `KNX*`, `Mqtt*`, `Airthing*`, `Gaiamesh*`, `IPS*`, `Pelican*`, `PolyLens*`, `VergeSense*`, `Siemens*` | C | Stub per AP-C2 |
| `Ticket*`, `Workorder*`, `Corrigo*`, `Maximo*`, `Mri*`, `Pms*` | C | Stub per AP-C3 |
| `Inspection*`, `Checklist*`, `RecordChecklist*`, `GlobalInspection*`, `GlobalChecklist*`, `ReportConditions*` | C | Stub per AP-C4 |
| `Inventory*` | C | Stub per AP-C8 |
| `ADC*` | C | Stub per AP-C9 |
| `History*`, `Analytics*`, `UserActionLog*` | C | Stub per AP-C6 |
| `Customer_Organisation*`, tenant-level `Property` (not AP-C1's PropertyService) | C | Stub per CP-2 |
| `io.sclera.offline.*`, `io.sclera.sockets.*`, `io.sclera.websocket.*`, `io.sclera.proxy.*`, `io.sclera.rabbitmq.*`, `io.sclera.startup.*`, `io.sclera.controller.touchscreen.*`, `io.sclera.controller.networktools.*`, `io.sclera.integration.*` | D | Edge-only - delete call site **from new repo only** |

### 15d. Standard Bucket-B copy pattern

```powershell
$missingFqn = "io.sclera.utils.SomeMissingHelper"
$relPath = ($missingFqn -replace "\.","\") + ".java"
$srcPath = "src\main\java\$relPath"
$destPath = "..\sclera-cloud-device-asset\src\main\java\$relPath"
$destDir = Split-Path $destPath -Parent
New-Item -ItemType Directory -Force -Path $destDir | Out-Null
Copy-Item -Path $srcPath -Destination $destPath
Add-Content -Path "..\sclera-cloud-device-asset\migration-notes\compile-fixes.md" -Value "| $(Get-Date -Format yyyy-MM-dd) | $missingFqn | B | copied from source | |" -Encoding utf8
```

### 15e. Standard Bucket-D call-site removal pattern

The call site is in an AP-C1 file in the new repo. Comment out or delete the offending import / field / method invocation. Add `// removed: edge-only dependency on <fqn> (Bucket D)` next to the deletion. Record in `migration-notes/known-breakages.md` if removing the call changes observable AP-C1 behavior.

### 15f. Iteration

- [ ] **Step 1: Re-run `mvn clean compile`.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
mvn -B -q clean compile 2>&1 | Tee-Object -FilePath "..\sclera-vdms-edge-server\target-tmp-compile-pass-N.log"
Pop-Location
```

- [ ] **Step 2: Re-extract missing symbols.**

```powershell
Select-String -Path "target-tmp-compile-pass-N.log" -Pattern "cannot find symbol|package .* does not exist" | ForEach-Object { $_.Line.Trim() } | Sort-Object -Unique
```

- [ ] **Step 3: Apply the classification table (15c) to each, copy / stub / delete accordingly.**

- [ ] **Step 4: Commit each wave with a clear message.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add -A
git commit -m "chore(compile-loop): pass N - copy 4 Bucket-B files, add 3 AP-C2 stubs, remove 2 Bucket-D call sites"
Pop-Location
```

- [ ] **Step 5: Repeat 15f Steps 1-4 until `mvn clean compile` reports BUILD SUCCESS.** Typical pass count: 5 to 15.

- [ ] **Step 6: When compile is green, confirm a fat JAR is produced.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
mvn -B -q clean package -DskipTests
Get-ChildItem target\*.jar | Select-Object Name, Length
Pop-Location
```

Expected: `sclera-cloud-device-asset.jar` written.

- [ ] **Step 7: Commit final compile-loop close-out.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add -A
git commit -m "chore(compile-loop): green - mvn clean package -DskipTests succeeds"
Pop-Location
```

---

## Task 16: Create the database and run a smoke boot (Phase 3)

- [ ] **Step 1: Create the new MySQL schema.**

```powershell
mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS sclera_cloud_device_asset CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -proot -e "SHOW DATABASES LIKE 'sclera_cloud_device_asset';"
```

Expected: one row containing `sclera_cloud_device_asset`.

- [ ] **Step 2: Boot the app in the foreground; let JPA auto-create tables.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
$env:DB_USER="root"; $env:DB_PASS="root"
mvn spring-boot:run 2>&1 | Tee-Object -FilePath "..\sclera-vdms-edge-server\target-tmp-boot-1.log"
```

Watch for `Started ScleraCloudDeviceAssetApplication in <time> seconds`.

- [ ] **Step 3: From a second terminal, exercise the basic endpoints.**

```powershell
Invoke-WebRequest "http://localhost:8085/swagger-ui.html" -UseBasicParsing | Select-Object StatusCode
Invoke-WebRequest "http://localhost:8085/v3/api-docs" -UseBasicParsing | Select-Object StatusCode
```

Expected: 200 for both.

- [ ] **Step 4: Confirm only AP-C1 tables were generated.**

```powershell
mysql -uroot -proot -e "USE sclera_cloud_device_asset; SHOW TABLES;"
```

Expected: tables including `device`, `asset_field`, `building`, `floor`, `location`, `location_history`, `device_specification`, `device_lifecycle_history`, `device_conditions`, `managed_software`, `ai_call_log`, `device_technician_ai_suggestion`, `document`, `media`, `note`, `asset_device_mapping`, `device_onboard_status`, `device_onboard_status_assignee`, `device_types`, `application_user`. No `alert_profile`, `ticket`, `inspection_*`, `integration_*`, `inventory_*` tables.

- [ ] **Step 5: Stop the app (Ctrl+C in the first terminal).**

- [ ] **Step 6: Append boot results to `migration-notes/smoke-checklist.md` and commit.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
Add-Content -Path "migration-notes\smoke-checklist.md" -Value "`n## Boot pass 1 ($(Get-Date -Format yyyy-MM-dd))`n`n- swagger-ui: 200`n- api-docs: 200`n- tables created: $(mysql -uroot -proot -N -e 'SHOW TABLES FROM sclera_cloud_device_asset;' | Measure-Object -Line | Select-Object -ExpandProperty Lines)`n" -Encoding utf8
git add migration-notes/smoke-checklist.md
git commit -m "chore: record successful Phase 3 smoke boot"
Pop-Location
```

---

## Task 17: Manual smoke checklist - create a device, observe a stub WARN

- [ ] **Step 1: Re-boot the app (Task 16 Step 2 if not already running).**

- [ ] **Step 2: POST a minimal device.**

```powershell
$payload = @{ device_name = "smoke-test-device-1"; category = "smoke" } | ConvertTo-Json
$resp = Invoke-WebRequest "http://localhost:8085/admin/device" -Method POST -ContentType "application/json" -Body $payload -UseBasicParsing
$resp.StatusCode
$resp.Content
```

If POST `/admin/device` requires JWT, you will see 401. Generate a token from the configured Keycloak issuer or temporarily set `spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration` for the seed run (note in `migration-notes/known-breakages.md`).

- [ ] **Step 3: GET the device by id.**

```powershell
Invoke-WebRequest "http://localhost:8085/admin/device/1" -UseBasicParsing | Select-Object -ExpandProperty Content
```

- [ ] **Step 4: Trigger an asset onboard flow and watch for stub WARN logs.**

```powershell
Select-String -Path "target-tmp-boot-1.log" -Pattern "STUB CALL:" | Select-Object -First 10
```

Expected: at least one WARN line per stub class touched.

- [ ] **Step 5: Commit smoke notes.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add migration-notes/
git commit -m "chore: complete Phase 3 manual smoke; stubs observed firing"
Pop-Location
```

---

## Task 18: Write the context-load smoke test

**Files:**
- Create: `Microservice/sclera-cloud-device-asset/src/test/java/io/sclera/ScleraCloudDeviceAssetApplicationTests.java`

- [ ] **Step 1: Write the test.**

```java
package io.sclera;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ScleraCloudDeviceAssetApplicationTests {

    @Test
    void contextLoads() {
        // assertion is implicit: if the context fails to load, the test fails
    }
}
```

Write with `Set-Content -Encoding utf8`.

- [ ] **Step 2: Run only this test.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
mvn -B test -Dtest=ScleraCloudDeviceAssetApplicationTests
Pop-Location
```

Expected: `Tests run: 1, Failures: 0`. If it fails, the error message names the bean that failed to wire — fix in `io.sclera.stubs/` or copy the missing class from source, then re-run.

- [ ] **Step 3: Commit.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/test/java/io/sclera/ScleraCloudDeviceAssetApplicationTests.java
git commit -m "test: add context-load smoke test (Phase 4 gate)"
Pop-Location
```

---

## Task 19: Write the stub-contract tests

**Files:**
- Create: `Microservice/sclera-cloud-device-asset/src/test/java/io/sclera/stubs/StubContractTests.java`

The test reflectively walks every bean in `io.sclera.stubs/` and asserts: (a) no method throws on default args, (b) a WARN was emitted.

- [ ] **Step 1: Write the test.**

```java
package io.sclera.stubs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class StubContractTests {

    @Autowired ApplicationContext ctx;
    private ListAppender<ILoggingEvent> appender;
    private Logger stubLogger;

    @BeforeEach
    void setUp() {
        stubLogger = (Logger) LoggerFactory.getLogger("io.sclera.stubs");
        appender = new ListAppender<>();
        appender.start();
        stubLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        stubLogger.detachAppender(appender);
    }

    @TestFactory
    Stream<DynamicTest> everyStubMethodDoesNotThrowAndLogsWarn() {
        String[] beans = ctx.getBeanNamesForType(Object.class);
        List<DynamicTest> tests = new ArrayList<>();
        for (String name : beans) {
            Object bean = ctx.getBean(name);
            if (!bean.getClass().getName().startsWith("io.sclera.stubs.")) continue;
            for (Method m : bean.getClass().getDeclaredMethods()) {
                if (m.getDeclaringClass() == Object.class) continue;
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
                if (!java.lang.reflect.Modifier.isPublic(m.getModifiers())) continue;
                tests.add(DynamicTest.dynamicTest(
                        bean.getClass().getSimpleName() + "#" + m.getName(),
                        () -> invokeAndAssert(bean, m)));
            }
        }
        return tests.stream();
    }

    private void invokeAndAssert(Object bean, Method m) {
        appender.list.clear();
        Object[] args = defaultArgs(m);
        try {
            m.invoke(bean, args);
        } catch (Throwable t) {
            fail("Stub " + bean.getClass().getSimpleName() + "#" + m.getName() + " threw " + t.getCause(), t);
            return;
        }
        assertTrue(appender.list.stream().anyMatch(e -> e.getLevel() == Level.WARN && e.getFormattedMessage().contains("STUB CALL:")),
                "Stub " + bean.getClass().getSimpleName() + "#" + m.getName() + " must emit a WARN with 'STUB CALL:'");
    }

    private Object[] defaultArgs(Method m) {
        Class<?>[] types = m.getParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) args[i] = defaultValue(types[i]);
        return args;
    }

    private Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class)    return (byte) 0;
        if (type == short.class)   return (short) 0;
        if (type == int.class)     return 0;
        if (type == long.class)    return 0L;
        if (type == float.class)   return 0f;
        if (type == double.class)  return 0d;
        if (type == char.class)    return '\0';
        return null;
    }
}
```

- [ ] **Step 2: Run.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
mvn -B test -Dtest=StubContractTests
Pop-Location
```

Expected: one DynamicTest per public method on each stub bean; all pass.

- [ ] **Step 3: Commit.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/test/java/io/sclera/stubs/StubContractTests.java
git commit -m "test(stubs): enforce no-throw + WARN-emitted contract across io.sclera.stubs/"
Pop-Location
```

---

## Task 20: Write @WebMvcTest slice tests (one per AP-C1 feature family)

**Files:** 12 test files under `Microservice/sclera-cloud-device-asset/src/test/java/io/sclera/controller/admin/`.

**Pattern:** mock the service layer with `@MockBean`, 1 positive + 1 negative test per controller. Worked example below for `DeviceControllerTest`; the other 11 follow the same shape.

- [ ] **Step 1: Write `DeviceControllerTest.java`.**

Read `DeviceController.java` and `DeviceService.java` in the new repo first to confirm exact method names and paths. Then:

```java
package io.sclera.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sclera.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeviceController.class)
@WithMockUser
class DeviceControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockBean DeviceService deviceService;

    @Test
    void getDeviceById_returnsOkForKnownId() throws Exception {
        when(deviceService.getDeviceById(anyLong())).thenReturn(new io.sclera.dto.DeviceDTO());
        mvc.perform(get("/admin/device/1"))
           .andExpect(status().is2xxSuccessful());
    }

    @Test
    void getDeviceById_returnsNotFoundForMissingId() throws Exception {
        when(deviceService.getDeviceById(anyLong()))
            .thenThrow(new io.sclera.exception.ResourceNotFoundException("not found"));
        mvc.perform(get("/admin/device/999999"))
           .andExpect(status().isNotFound());
    }
}
```

If `DeviceService.getDeviceById` does not exist, substitute the actual method name from `DeviceService.java`. Same for `DeviceDTO` and `ResourceNotFoundException`. The test asserts wiring (controller registered, JSON returned), not business logic.

- [ ] **Step 2: Run `DeviceControllerTest`.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
mvn -B test -Dtest=DeviceControllerTest
Pop-Location
```

Expected: 2 tests pass.

- [ ] **Step 3: Replicate the same pattern for the remaining 11 controllers.**

For each row below, copy `DeviceControllerTest.java`, change four substitution slots — controller class, service bean type, endpoint URL, DTO type. Read the actual `@RequestMapping`/`@GetMapping` paths from the controller and the actual service method name from the service before substituting.

| Test file | Controller | Service mocked | Positive endpoint | Negative case |
|---|---|---|---|---|
| `AssetFieldControllerTest.java` | AssetFieldController | (read controller for service field) | actual GET path | malformed body → 400 or unknown id → 404 |
| `AssetOnboardControllerTest.java` | AssetOnboardController | AssetOnboardService | actual POST path | invalid payload → 400 |
| `BuildingControllerTest.java` | BuildingController | BuildingService | `GET /admin/building/1` | missing id → 404 |
| `ChatGPTControllerTest.java` | ChatGPTController | ChatGPTService | actual POST path | invalid payload → 400 |
| `DeviceConditionsControllerTest.java` | DeviceConditionsController | DeviceConditionsService | actual GET path | missing id → 404 |
| `DeviceLifecycleHistoryControllerTest.java` | DeviceLifecycleHistoryController | DeviceLifecycleHistoryService | actual GET path | missing id → 404 |
| `DeviceSpecificationControllerTest.java` | DeviceSpecificationController | DeviceSpecificationService | actual GET path | missing id → 404 |
| `DocumentControllerTest.java` | DocumentController | DocumentService | actual GET path | missing id → 404 |
| `LocationControllerTest.java` | LocationController | LocationService | actual GET path | missing id → 404 |
| `ManagedSoftwareControllerTest.java` | ManagedSoftwareController | ManagedSoftwareService | actual GET path | missing id → 404 |
| `NoteControllerTest.java` | NoteController | NotesService | actual GET path | missing id → 404 |

If a controller never returns 404 for a missing id (returns an empty list instead), use the malformed-body → 400 fallback for the negative case.

- [ ] **Step 4: Run all slice tests together.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
mvn -B test -Dtest='*ControllerTest'
Pop-Location
```

Expected: 12 classes, 24 tests, all pass.

- [ ] **Step 5: Commit the slice test pack.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/test/java/io/sclera/controller/admin/
git commit -m "test(controller): add @WebMvcTest slice tests, one per AP-C1 controller"
Pop-Location
```

---

## Task 21: Write @DataJpaTest for DeviceRepository

**Files:**
- Create: `Microservice/sclera-cloud-device-asset/src/test/java/io/sclera/Repository/DeviceRepositoryTest.java`

- [ ] **Step 1: Write the test.**

```java
package io.sclera.Repository;

import io.sclera.models.Device;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class DeviceRepositoryTest {

    @Autowired DeviceRepository deviceRepository;

    @Test
    void canSaveAndFindADevice() {
        Device d = new Device();
        // Populate only NOT NULL fields. Read Device.java for actual @Column(nullable=false) fields.
        Device saved = deviceRepository.save(d);
        assertNotNull(saved.getId(), "id assigned after save");
        assertTrue(deviceRepository.findById(saved.getId()).isPresent(),
                "saved device retrievable by id");
    }
}
```

- [ ] **Step 2: Run.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
mvn -B test -Dtest=DeviceRepositoryTest
Pop-Location
```

Expected: 1 test passes. If `Device.java` has `@Column(nullable=false)` fields with no defaults, the save fails — set those fields explicitly in the test and re-run.

- [ ] **Step 3: If the test fails because of MySQL-specific native SQL in `DeviceRepository` that H2 cannot parse, switch to Testcontainers.**

Add to the new repo's `pom.xml`:

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <version>1.19.7</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.19.7</version>
    <scope>test</scope>
</dependency>
```

Then update `DeviceRepositoryTest`:

```java
@DataJpaTest
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
@org.testcontainers.junit.jupiter.Testcontainers
class DeviceRepositoryTest {

    @org.testcontainers.junit.jupiter.Container
    static org.testcontainers.containers.MySQLContainer<?> mysql =
        new org.testcontainers.containers.MySQLContainer<>("mysql:8")
            .withDatabaseName("sclera_cloud_device_asset_test");

    @org.springframework.test.context.DynamicPropertySource
    static void props(org.springframework.test.context.DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
    }
    // ... rest unchanged
}
```

Log the fallback in `migration-notes/compile-fixes.md`.

- [ ] **Step 4: Commit.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git add src/test/java/io/sclera/Repository/DeviceRepositoryTest.java
git add pom.xml   # only if Testcontainers was added
git commit -m "test(repo): add DeviceRepositoryTest with H2 or Testcontainers fallback"
Pop-Location
```

---

## Task 22: Final acceptance check + invariant verification

- [ ] **Step 1: Run the full test pack.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
mvn -B clean test
Pop-Location
```

Expected: BUILD SUCCESS; tests = (1 context-load) + (N stub dynamic tests) + (24 slice tests) + (1 repo test). All pass.

- [ ] **Step 2: Run a full clean build incl. package.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
mvn -B clean package
Pop-Location
```

Expected: BUILD SUCCESS; `target/sclera-cloud-device-asset.jar` produced.

- [ ] **Step 3: Confirm source repo unchanged.**

Run from inside `sclera-vdms-edge-server`:

```powershell
git status
git diff --stat
```

Expected: `nothing to commit, working tree clean` on tracked files (untracked files in `docs/superpowers/specs/` and `docs/superpowers/plans/` are OK — the design + plan we just authored). No tracked file should be modified.

- [ ] **Step 4: Confirm Swagger lists only AP-C1 endpoints.**

```powershell
$json = Invoke-WebRequest "http://localhost:8085/v3/api-docs" -UseBasicParsing
$paths = ($json.Content | ConvertFrom-Json).paths.PSObject.Properties.Name
$paths | Sort-Object
```

Expected: every path is one of the AP-C1 prefixes in spec §6.2. Update `migration-notes/endpoint-diff.md`.

- [ ] **Step 5: Tag the milestone.**

```powershell
Push-Location "..\sclera-cloud-device-asset"
git tag -a phase-1-complete -m "AP-C1 extraction Phase 1 complete: compile green, smoke boots, tests pass"
Pop-Location
```

- [ ] **Step 6: Walk the spec §12 checklist and tick each item with evidence (mvn output, SHOW TABLES, git status).** Append any caveats discovered to `migration-notes/known-breakages.md`.

---

## Acceptance criteria (mirror of spec §12)

- [ ] New repo `Microservice/sclera-cloud-device-asset/` exists as a sibling of source, initialized as a fresh git repo.
- [ ] `mvn clean compile` green.
- [ ] `mvn spring-boot:run` boots without exception against a fresh `sclera_cloud_device_asset` MySQL schema.
- [ ] Swagger UI at `/swagger-ui.html` lists only AP-C1 endpoints.
- [ ] `mvn test` green (context-load smoke + stub-contract + slice tests + repo smoke).
- [ ] All six `migration-notes/*.md` files written and reflect the actual choices made during execution.
- [ ] Source repo `sclera-vdms-edge-server` is unchanged (`git diff` against the starting commit shows no changes to its tracked files).
