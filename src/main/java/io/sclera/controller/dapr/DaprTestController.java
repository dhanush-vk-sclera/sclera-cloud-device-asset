package io.sclera.controller.dapr;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dapr")
public class DaprTestController {

    private final DaprClient daprClient;

    public DaprTestController(DaprClient daprClient) {
        this.daprClient = daprClient;
    }

    @GetMapping("/test")
    public String test() {
        byte[] result = daprClient
            .invokeMethod("vdms-service", "getVdmsInfo", null, HttpExtension.GET, byte[].class)
            .block();
        return result != null ? new String(result) : "{}";
    }
}
