package com.loopers.domain.payment;

public interface PaymentEventPublisher {
    void publish(PaymentEvent.PaymentSucceededEvent e);
    void publish(PaymentEvent.PaymentFailedEvent e);
}
