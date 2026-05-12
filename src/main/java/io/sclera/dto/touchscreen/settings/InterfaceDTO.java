package io.sclera.dto.touchscreen.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterfaceDTO {
    private String interface_name;
    private Boolean isActive;
    private String gateway_ip;
    private String ip;
    private String mac_address;
    private String netmask;
    private Integer interface_origin;

    public InterfaceDTO(String interface_name) {
        this.interface_name = interface_name;
    }

}
