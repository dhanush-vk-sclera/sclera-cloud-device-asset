package io.sclera.dto.touchscreen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteAccessAdditionalInfoDTO {
    private Integer public_port;
    private Integer private_port;
    private String ip_address;
    private String network_name;
    private String docker_internal_ip;
    private String mac_address;
    private String methodName;
}