package com.loopers.infrastructure.monitoring;

import com.loopers.domain.monitoring.activity.ActivityPublisher;
import com.loopers.shared.logging.Envelope;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityPublisherImpl implements ActivityPublisher {
    private final ApplicationEventPublisher publisher;
    public <T> void publish(Envelope<T> env) { publisher.publishEvent(env); }

}
