package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Payment")
class PaymentTest {

    @Nested
    @DisplayName("[결제 생성]")
    class Create {
        @Test
        @DisplayName("[성공] 결제를 생성하면 상태는 PAID이다.")
        void success_createPayment() {
            Payment p = Payment.create(1L, 1L, 1000L, Payment.Method.POINT);
            assertThat(p.getStatus()).isEqualTo(Payment.Status.PAID);
            assertThat(p.getAmount()).isEqualTo(1000L);
        }
    }

    @Nested
    @DisplayName("[결제 취소]")
    class Cancel {
        @Test
        @DisplayName("[성공] 결제를 취소하면 상태가 CANCELLED로 변경된다.")
        void success_cancel() {
            Payment p = Payment.create(1L, 1L, 1000L, Payment.Method.POINT);
            p.cancel();
            assertThat(p.getStatus()).isEqualTo(Payment.Status.CANCELLED);
        }

        @Test
        @DisplayName("[실패] 이미 취소된 결제를 다시 취소하면 예외 발생")
        void failure_cancelAlreadyCancelled() {
            Payment p = Payment.create(1L, 1L, 1000L, Payment.Method.POINT);
            p.cancel();
            assertThrows(CoreException.class, p::cancel);
        }
    }
}
