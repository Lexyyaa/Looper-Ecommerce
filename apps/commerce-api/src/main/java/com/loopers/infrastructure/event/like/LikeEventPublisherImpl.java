package com.loopers.infrastructure.event.like;

import com.loopers.domain.like.LikeEvent;
import com.loopers.domain.like.LikeEventPublisher;
import com.loopers.shared.event.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventPublisherImpl implements LikeEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void like(LikeEvent.Added event) {
        applicationEventPublisher.publishEvent(Envelope.of(event.loginId(),event));
    }

    @Override
    public void unlike(LikeEvent.Removed event) {
        applicationEventPublisher.publishEvent(Envelope.of(event.loginId(),event));
    }
}
