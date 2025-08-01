package com.loopers.domain.payment;

import java.time.ZonedDateTime;

public class PaymentInfo {
    public record CreatePayment(
            Long paymentId,
            Long userId,
            Long orderId,
            Long amount,
            Payment.Method method,
            Payment.Status status,
            ZonedDateTime createdAt
    ) {
        public static CreatePayment from(Payment payment) {
            return new CreatePayment(
                    payment.getId(),
                    payment.getUserId(),
                    payment.getOrderId(),
                    payment.getAmount(),
                    payment.getMethod(),
                    payment.getStatus(),
                    payment.getCreatedAt()
            );
        }
    }
}
