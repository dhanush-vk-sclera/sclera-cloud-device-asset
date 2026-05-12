package io.sclera.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceInstalledAppsDTO {
    private String name;
    private String version;
    private String vendor;
}