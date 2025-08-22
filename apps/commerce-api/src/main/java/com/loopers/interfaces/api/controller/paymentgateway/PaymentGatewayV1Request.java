package com.loopers.interfaces.api.controller.paymentgateway;

import java.math.BigDecimal;

public class PaymentGatewayV1Request {
    public record TransactionInfo(
            String transactionKey,
            String orderId,
            String cardType,
            String cardNo,
            BigDecimal amount,
            String status,
            String reason
    ) {
    }
}
