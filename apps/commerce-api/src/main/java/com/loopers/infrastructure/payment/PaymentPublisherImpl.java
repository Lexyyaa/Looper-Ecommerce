package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEvent;
import com.loopers.domain.payment.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentPublisherImpl implements PaymentEventPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(PaymentEvent.PaymentSucceededEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public void publish(PaymentEvent.PaymentFailedEvent event) {
        publisher.publishEvent(event);
    }
}
