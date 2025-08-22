package com.loopers.domain.paymentgateway;

import com.loopers.domain.payment.Payment;

public record PaymentDetail(
        String transactionKey,
        String orderId,
        Long amount,
        Payment.Status status,
        String reason
) {
}
