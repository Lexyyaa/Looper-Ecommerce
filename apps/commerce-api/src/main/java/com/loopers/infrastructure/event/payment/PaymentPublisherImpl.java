package com.loopers.infrastructure.event.payment;

import com.loopers.domain.payment.PaymentEvent;
import com.loopers.domain.payment.PaymentEventPublisher;
import com.loopers.shared.event.Envelope;
import com.loopers.shared.logging.SystemActors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentPublisherImpl implements PaymentEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(PaymentEvent.PaymentSucceededEvent event) {
        applicationEventPublisher.publishEvent(Envelope.of(SystemActors.PG_CALLBACK,event));
    }

    @Override
    public void publish(PaymentEvent.PaymentFailedEvent event) {
        applicationEventPublisher.publishEvent(Envelope.of(SystemActors.PG_CALLBACK,event));
    }
}
