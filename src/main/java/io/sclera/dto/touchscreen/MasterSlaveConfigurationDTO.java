package io.sclera.dto.touchscreen;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterSlaveConfigurationDTO {
    private Integer is_master;
    private String secondary_device_id;
    private String slave_ip;
    private String master_ip;
    private String vdms_id;
    private Integer has_secondary_device;
}