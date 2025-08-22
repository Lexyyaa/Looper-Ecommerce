package com.loopers.domain.payment;

import com.loopers.interfaces.api.controller.payment.PaymentV1Request;
import com.loopers.interfaces.api.controller.paymentgateway.PaymentGatewayV1Request;

import java.math.BigDecimal;

public class PaymentCommand {
    public record CreatePayment(
            String loginId,
            Long orderId,
            Long amount,
            Payment.Method method,
            CardPaymentDetails details
    ) {
        public static CreatePayment create(
                PaymentV1Request.CreatePayment request,
                Long orderId
        ) {
            return new CreatePayment(
                    request.loginId(),
                    orderId,
                    request.amount(),
                    request.method(),
                    new CardPaymentDetails(
                            request.details().cardType(),
                            request.details().cardNo()
                    )
            );
        }
    }

    //TODO. 카드결제 DTO 추후 추상체와 구현체 분리
    public record CardPaymentDetails(
            String cardType,
            String cardNo
    ) {}

    public record SyncPayment(
            String transactionKey,
            String orderId,
            BigDecimal amount,
            String status,
            String reason
    ){
        public static SyncPayment create(PaymentGatewayV1Request.TransactionInfo request ) {
            return new SyncPayment(
                    request.transactionKey(),
                    request.orderId(),
                    request.amount(),
                    request.status(),
                    request.reason()
            );
        }

        public static SyncPayment from(Payment payment) {
            return new SyncPayment(
                    payment.getTxKey(),
                    payment.getOrderId().toString(),
                    BigDecimal.valueOf(payment.getAmount()),
                    payment.getStatus().toString(),
                    payment.getFailReason()
            );
        }
    }

    public record CancelPayment(
            String loginId,
            Long orderId,
            Long paymentId
    ) {}

}
