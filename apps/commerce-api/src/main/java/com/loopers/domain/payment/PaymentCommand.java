package com.loopers.domain.payment;

public class PaymentCommand {
    public record CreatePayment(
            String loginId,
            Long orderId,
            Long amount,
            String method // "POINT" or "CREDIT"
    ) {}

    public record CancelPayment(
            String loginId,
            Long orderId,
            Long paymentId
    ) {}
}
