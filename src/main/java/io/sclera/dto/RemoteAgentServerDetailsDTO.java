package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemoteAgentServerDetailsDTO {

    private String id;
    private String name;
    private Integer exposePort;
    private Integer gatewayPort;
    private Integer tunnelTcpPort;
    private Integer tunnelKcpPort;
    private Integer tunnelQuicPort;
    private Integer vhttpPort;
    private Integer webserverPort;

    private String location;
    private String latitude;
    private String longitude;
    private String timezone;

    private String publicIp;
    private String privateIp;
    private String publicDns;
    private String privateDns;

    private Integer serverType;
    private String token;
    private String password;
    private String customDomain;

    private List<RemoteDesktopSessionDTO> sessions;
}
