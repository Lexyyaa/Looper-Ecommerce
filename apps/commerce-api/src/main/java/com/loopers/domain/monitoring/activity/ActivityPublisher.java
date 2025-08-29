package com.loopers.domain.monitoring.activity;

import com.loopers.shared.logging.Envelope;

public interface ActivityPublisher {
    public <T> void publish(Envelope<T> env);
}
