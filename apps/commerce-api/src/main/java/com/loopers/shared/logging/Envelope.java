package com.loopers.shared.logging;

import java.time.Instant;
import java.util.UUID;

public record Envelope<T>(
        String id,
        Instant at,
        String actorId,
        T payload
) {
    public static <T> Envelope<T> of(
            String actorId,
            T payload) {
        return new Envelope<>(
                UUID.randomUUID().toString(),
                Instant.now(),
                actorId,
                payload
        );
    }
}
