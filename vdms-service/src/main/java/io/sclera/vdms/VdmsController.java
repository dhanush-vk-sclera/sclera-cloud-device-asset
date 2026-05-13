package io.sclera.vdms;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VdmsController {

    @GetMapping("/getVdmsInfo")
    public Map<String, String> getVdmsInfo() {
        return Map.of(
            "vdmsId", "vdms-001",
            "name",   "VDMS Edge Node",
            "status", "active"
        );
    }
}
