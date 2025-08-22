package com.loopers.domain.paymentgateway;

public record PaymentGatewayResponse(
        String txKey,
        String status,
        String reason
) {
}
