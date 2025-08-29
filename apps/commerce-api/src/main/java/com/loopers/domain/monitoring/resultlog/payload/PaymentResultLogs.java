package com.loopers.domain.monitoring.resultlog.payload;

import com.loopers.domain.monitoring.resultlog.ResultLogPayload;
import com.loopers.domain.payment.Payment;

public class PaymentResultLogs {
    public record PaymentSucceeded(Long orderId, Long paymentId, Long amount, Payment.Method method)
            implements ResultLogPayload {}

    public record PaymentFailed(Long orderId, Long paymentId, Long amount, Payment.Method method, String reason)
            implements ResultLogPayload {}
}
