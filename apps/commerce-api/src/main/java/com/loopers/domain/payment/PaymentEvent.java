package com.loopers.domain.payment;

import com.loopers.domain.order.Order;

public class PaymentEvent {
    public record PaymentSucceededEvent(
            Order order
    ) {

    }
    public record PaymentFailedEvent(
            Order order
    ) {

    }
}
