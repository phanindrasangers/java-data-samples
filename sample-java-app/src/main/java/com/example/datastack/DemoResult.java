package com.example.datastack;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DemoResult(
        boolean success,
        Instant time,
        UUID eventId,
        Map<String, String> cassandra,
        Map<String, String> keydb,
        Map<String, String> kafka,
        String error) {
    static DemoResult empty() {
        return new DemoResult(false, Instant.EPOCH, null, Map.of(), Map.of(), Map.of(), "demo has not run yet");
    }
}
