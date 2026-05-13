package io.sclera.service;

import io.sclera.dto.touchscreen.settings.DockerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to edge-D */
@Service
public class DockerService {

    private static final Logger log = LoggerFactory.getLogger(DockerService.class);

    public DockerDTO checkIfHostNetworkPresentByNetworkOrigin(Integer networkOrigin) {
        log.warn("STUB: checkIfHostNetworkPresentByNetworkOrigin called");
        return null;
    }

    public String getDockerInternalIp(String dockerName) { log.warn("STUB: getDockerInternalIp"); return null; }
    public String getGatewayIp(String dockerName) { log.warn("STUB: getGatewayIp"); return null; }

}
