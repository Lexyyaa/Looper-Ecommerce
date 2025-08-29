package com.loopers.domain.monitoring.resultlog;

import com.loopers.shared.logging.Envelope;

public interface ResultLogPublisher {
    public void publish(Envelope<? extends ResultLogPayload> e);
}
