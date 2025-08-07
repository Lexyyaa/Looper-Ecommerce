package com.loopers.interfaces.api.controller.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentInfo;

import java.time.ZonedDateTime;

public class PaymentV1Response {
    public record CreatePayment(
            Long paymentId,
            Long userId,
            Long orderId,
            Long amount,
            Payment.Method method,
            Payment.Status status,
            ZonedDateTime createdAt
    ) {
        public static CreatePayment from(PaymentInfo.CreatePayment info) {
            return new CreatePayment(
                    info.paymentId(),
                    info.userId(),
                    info.orderId(),
                    info.amount(),
                    info.method(),
                    info.status(),
                    info.createdAt()
            );
        }
    }

    public record CancelPayment(
            Long paymentId,
            Long orderId,
            Long userId,
            String method,
            String status,
            ZonedDateTime updatedAt
    ) {
        public static CancelPayment from(PaymentInfo.CancelPayment info) {
            return new CancelPayment(
                    info.paymentId(),
                    info.orderId(),
                    info.userId(),
                    info.method().name(),
                    info.status().name(),
                    info.updatedAt()
            );
        }
    }
}
