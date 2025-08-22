package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class PaymentProcessorFactory {

    private final PointPaymentProcessor pointPaymentProcessor;
    private final CardPaymentProcessor cardPaymentProcessor;

    public PaymentProcessor of(Payment.Method method) {
        return switch (method) {
            case POINT -> pointPaymentProcessor;
            case CARD -> cardPaymentProcessor;
        };
    }
}
