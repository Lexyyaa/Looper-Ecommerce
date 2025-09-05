package com.loopers.shared.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.time.Instant;
import java.util.UUID;

/**
 * 모든 이벤트 공통 봉투.
 * - id: 멱등/추적 키
 * - at: 발생 시각
 * - actorId: 행위 주체
 * - payload: 실제 데이터(행위/도메인/집계 등)
 *
 * 💡 ResolvableTypeProvider: 스프링 이벤트가 제네릭까지 매칭 가능하게 함.
 *    → @EventListener(Envelope<LikeEvent.Added>) 같은 시그니처가 정확히 호출됨.
 */
public record Envelope<T>(
        String id,
        Instant at,
        String actorId,
        T payload
) implements ResolvableTypeProvider {

    public static <T> Envelope<T> of(String actorId, T payload) {
        return new Envelope<>(
                UUID.randomUUID().toString(),
                Instant.now(),
                actorId,
                payload
        );
    }

    @Override
    @JsonIgnore
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                Envelope.class,
                ResolvableType.forInstance(payload)
        );
    }
}
