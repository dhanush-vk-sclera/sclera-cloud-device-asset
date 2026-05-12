package io.sclera.dto.touchscreen;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkToolsDTO {

    private String username;
    private String vdmsId;
    private String networkName;
    private String input;
    private String privateIp;
    private String publicIp;
    private Integer privatePort;
    private Integer publicPort;
    private String dockerIp;
    private Integer network_origin;

    @JsonAlias({"isSecure"})
    private Boolean isSecure;
}
