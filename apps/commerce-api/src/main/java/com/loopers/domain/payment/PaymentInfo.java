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
            String idempotencyKey,
            String transactionKey,
            ZonedDateTime updatedAt
    ) {
        public static CreatePayment from(Payment payment) {
            return new CreatePayment(
                    payment.getId(),
                    payment.getUserId(),
                    payment.getOrderId(),
                    payment.getAmount(),
                    payment.getMethod(),
                    payment.getStatus(),
                    payment.getIdempotencyKey(),
                    payment.getTxKey(),
                    payment.getUpdatedAt()
            );
        }
    }

    public record CancelPayment(
            Long paymentId,
            Long orderId,
            Long userId,
            Payment.Method method,
            Payment.Status status,
            ZonedDateTime updatedAt
    ) {
        public static CancelPayment from(Payment payment) {
            return new CancelPayment(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getMethod(),
                    payment.getStatus(),
                    payment.getUpdatedAt()
            );
        }
    }
}
