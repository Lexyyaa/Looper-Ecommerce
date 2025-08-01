package com.loopers.interfaces.api.controller.payment;

public class PaymentRequest {
    public record CreatePayment(
            String loginId,
            Long amount,
            String method // "POINT" or "CREDIT"
    ) {}
}
