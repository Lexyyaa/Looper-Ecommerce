package com.loopers.interfaces.api.controller.payment;

public class PaymentV1Request {
    public record CreatePayment(
            String loginId,
            Long amount,
            String method
    ) {}

    public record CancelPayment(
            String loginId
    ) {}
}
