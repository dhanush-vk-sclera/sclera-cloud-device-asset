package io.sclera.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.Objects;

public final class StubLog {
    private static final Logger log = LoggerFactory.getLogger("io.sclera.stubs");

    private StubLog() {}

    public static void warn(Class<?> stubClass, String method, String targetMicroservice, Object... args) {
        log.warn("STUB CALL: class={} method={} target_microservice={} args_hash={}",
                stubClass.getName(), method, targetMicroservice, Objects.hash(Arrays.deepHashCode(args)));
    }
}