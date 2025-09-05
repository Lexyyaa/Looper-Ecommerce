package com.loopers.infrastructure.event.like;

import com.loopers.domain.like.LikeActivityPayload;
import com.loopers.domain.like.LikeActivityPublisher;
import com.loopers.shared.event.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeActivityPublisherImpl implements LikeActivityPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void like(LikeActivityPayload.LikeAdded event) {
        applicationEventPublisher.publishEvent(Envelope.of(event.loginId(),event));
    }

    @Override
    public void unlike(LikeActivityPayload.LikeRemoved event) {
        applicationEventPublisher.publishEvent(Envelope.of(event.loginId(),event));
    }
}
