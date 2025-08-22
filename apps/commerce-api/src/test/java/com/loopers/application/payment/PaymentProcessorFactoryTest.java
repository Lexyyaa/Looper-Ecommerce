package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("PaymentProcessorFactory 단위 테스트 ")
public class PaymentProcessorFactoryTest {

    @Test
    @DisplayName("[성공] CARD 타입에 대해 CardPaymentProcessor 를 반환한다")
    void success_getProcessor_whenCard() {
        CardPaymentProcessor card = mock(CardPaymentProcessor.class);
        PointPaymentProcessor point = mock(PointPaymentProcessor.class);

        PaymentProcessorFactory factory = new PaymentProcessorFactory(point, card);

        PaymentProcessor selected = factory.of(Payment.Method.CARD);

        assertThat(selected).isSameAs(card);
    }

    @Test
    @DisplayName("[성공] POINT 타입에 대해 PointPaymentProcessor 를 반환한다")
    void success_getProcessor_whenPoint() {
        CardPaymentProcessor card = mock(CardPaymentProcessor.class);
        PointPaymentProcessor point = mock(PointPaymentProcessor.class);

        PaymentProcessorFactory factory = new PaymentProcessorFactory(point, card);

        PaymentProcessor selected = factory.of(Payment.Method.POINT);

        assertThat(selected).isSameAs(point);
    }
}
