package com.loopers.domain.paymentgateway;

public record PaymentGatewayRequest(
        String orderId,
        String cardType,
        String cardNo,
        String amount,
        String callbackUrl,
        String idempotencyKey) {
}
