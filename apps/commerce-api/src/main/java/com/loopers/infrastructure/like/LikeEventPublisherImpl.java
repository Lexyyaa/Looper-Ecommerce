package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEvent;
import com.loopers.domain.like.LikeEventPublisher;
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
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void unlike(LikeEvent.Removed event) {
        applicationEventPublisher.publishEvent(event);
    }
}
