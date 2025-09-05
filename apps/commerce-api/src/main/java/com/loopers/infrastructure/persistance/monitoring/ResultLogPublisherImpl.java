package com.loopers.infrastructure.persistance.monitoring;

import com.loopers.domain.monitoring.resultlog.ResultLogPayload;
import com.loopers.domain.monitoring.resultlog.ResultLogPublisher;
import com.loopers.shared.event.Envelope;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResultLogPublisherImpl implements ResultLogPublisher {
    private final ApplicationEventPublisher publisher;
    public void publish(Envelope<? extends ResultLogPayload> e) { publisher.publishEvent(e); }
}
