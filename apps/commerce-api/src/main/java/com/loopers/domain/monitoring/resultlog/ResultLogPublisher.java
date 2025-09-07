package com.loopers.domain.monitoring.resultlog;

import com.loopers.shared.event.Envelope;

public interface ResultLogPublisher {
    void publish(Envelope<? extends ResultLogPayload> e);
}
