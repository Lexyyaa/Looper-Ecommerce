package com.loopers.domain.payment;

public class PaymentCommand {
    public record CreatePayment(
            String loginId,
            Long orderId,
            Long amount,
            String method
    ) {}

    public record CancelPayment(
            String loginId,
            Long orderId,
            Long paymentId
    ) {}
}
