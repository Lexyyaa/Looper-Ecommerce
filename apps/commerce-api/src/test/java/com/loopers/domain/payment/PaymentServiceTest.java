package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Nested
    @DisplayName("[결제 저장]")
    class Save {

        @Test
        @DisplayName("[성공] 결제를 저장하고 저장된 엔티티를 반환한다.")
        void success_savePayment() {
            Payment payment = Payment.create(1L, 1L, 1000L, Payment.Method.POINT);
            when(paymentRepository.save(payment)).thenReturn(payment);

            Payment result = paymentService.save(payment);

            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(1000L);
            assertThat(result.getMethod()).isEqualTo(Payment.Method.POINT);

            verify(paymentRepository).save(payment);
        }

        @Test
        @DisplayName("[실패] 저장 과정에서 예외가 발생하면 INTERNAL_ERROR 에러를 반환한다.")
        void failure_savePayment_repositoryThrows() {
            Payment payment = Payment.create(1L, 1L, 1000L, Payment.Method.POINT);
            when(paymentRepository.save(payment))
                    .thenThrow(new CoreException(ErrorType.INTERNAL_ERROR, "저장 실패"));

            CoreException ex = assertThrows(CoreException.class, () -> paymentService.save(payment));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
            verify(paymentRepository).save(payment);
        }
    }
}
