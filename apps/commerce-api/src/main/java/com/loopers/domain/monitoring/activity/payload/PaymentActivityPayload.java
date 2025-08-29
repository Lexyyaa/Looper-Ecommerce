package com.loopers.domain.monitoring.activity.payload;

import com.loopers.domain.monitoring.activity.ActivityPayload;
import com.loopers.domain.payment.Payment;

public class PaymentActivityPayload {

    public record PayRequested (
            Long orderId,
            long amount,
            Payment.Method method
    )implements ActivityPayload {
    }

    public record PayCancelRequested (
            Long orderId,
            Long paymentId
    ) implements ActivityPayload {
    }
}
