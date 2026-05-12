# Compile-pass log

Running log of compile errors and resolutions during Phase 2.

| Date | Missing symbol | Classification (B/C/D) | Action | Notes |
|---|---|---|---|---|
## Pass 1 - initial compile

Unique missing-symbol error lines: 100

Note: also stripped a UTF-8 BOM from src/main/java/io/sclera/ScleraCloudDeviceAssetApplication.java (artifact of T7 PowerShell Set-Content default encoding) so javac could proceed past the entry-point file and surface the real error landscape.

### Sample (first 20)

```
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/AssetDeviceMapping.java:[3,45] package io.sclera.dto.touchscreen.assetmapper does not exist
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2733,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2788,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2811,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2841,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2845,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2849,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2852,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2856,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2860,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2864,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2868,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2871,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2877,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2881,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2884,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2887,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2890,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2896,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2899,17] cannot find symbol
```

