package io.sclera.service.touchscreen;

import io.sclera.dto.touchscreen.settings.VdmsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to edge-D */
@Service
public class VdmsService {

    private static final Logger log = LoggerFactory.getLogger(VdmsService.class);

    public Integer getIsMaster() {
        log.warn("STUB: getIsMaster called");
        return null;
    }

    public String getVDMSId() {
        log.warn("STUB: getVDMSId called");
        return null;
    }

    public VdmsDTO getVDMSDetails() {
        log.warn("STUB: getVDMSDetails called");
        return null;
    }
}
