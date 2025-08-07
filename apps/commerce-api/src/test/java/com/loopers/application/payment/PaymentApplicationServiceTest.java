package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentApplicationService")
public class PaymentApplicationServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private OrderService orderService;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentApplicationService paymentApplicationService;

    @Nested
    @DisplayName("[결제 요청]")
    class CreatePayment {

        @Test
        @DisplayName("[성공] 결제를 정상 생성한다.")
        void success_createPayment() {
            PaymentCommand.CreatePayment cmd =
                    new PaymentCommand.CreatePayment("loginId", 1L, 1000L, "POINT");

            User user = User.builder().id(10L).point(5000L).build();
            Order order = Order.create(user.getId(), 1000L);

            when(userService.getUser("loginId")).thenReturn(user);
            when(orderService.getOrder(1L)).thenReturn(order);
            Payment saved = Payment.create(user.getId(), order.getId(), 1000L, Payment.Method.POINT);
            when(paymentService.save(any(Payment.class))).thenReturn(saved);

            PaymentInfo.CreatePayment result = paymentApplicationService.createPayment(cmd);

            assertThat(result.userId()).isEqualTo(10L);
            assertThat(result.amount()).isEqualTo(1000L);
            verify(userService).getUser("loginId");
            verify(orderService).getOrder(1L);
            verify(orderService).saveOrder(order);
            verify(paymentService).save(any(Payment.class));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자로 결제 요청 시 NOT_FOUND 예외 발생")
        void failure_userNotFound() {
            PaymentCommand.CreatePayment cmd =
                    new PaymentCommand.CreatePayment("loginId", 1L, 1000L, "POINT");
            when(userService.getUser("loginId"))
                    .thenThrow(new CoreException(ErrorType.NOT_FOUND, "사용자 없음"));

            CoreException ex = assertThrows(CoreException.class,
                    () -> paymentApplicationService.createPayment(cmd));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 주문으로 결제 요청 시 NOT_FOUND 예외 발생")
        void failure_orderNotFound() {
            PaymentCommand.CreatePayment cmd =
                    new PaymentCommand.CreatePayment("loginId", 1L, 1000L, "POINT");
            User user = User.builder().id(10L).build();
            when(userService.getUser("loginId")).thenReturn(user);
            when(orderService.getOrder(1L))
                    .thenThrow(new CoreException(ErrorType.NOT_FOUND, "주문 없음"));

            CoreException ex = assertThrows(CoreException.class,
                    () -> paymentApplicationService.createPayment(cmd));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 이미 결제된 주문 → CONFLICT")
        void failure_orderAlreadyConfirmed() {
            // given
            PaymentCommand.CreatePayment cmd =
                    new PaymentCommand.CreatePayment("loginId", 1L, 1000L, "POINT");
            User user = User.builder().id(10L).build();
            Order order = Order.create(user.getId(), 1000L);
            order.confirm(); // 이미 결제됨

            when(userService.getUser("loginId")).thenReturn(user);
            when(orderService.getOrder(1L)).thenReturn(order);

            // when
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> paymentApplicationService.createPayment(cmd));

            // then
            assertThat(ex.getMessage()).contains("이미 결제된 주문");
        }

        @Test
        @DisplayName("[실패] 포인트 부족 → BAD_REQUEST")
        void failure_insufficientPoint() {
            PaymentCommand.CreatePayment cmd =
                    new PaymentCommand.CreatePayment("loginId", 1L, 5000L, "POINT");
            User user = User.builder().id(10L).point(1000L).build();
            Order order = Order.create(user.getId(), 5000L);

            when(userService.getUser("loginId")).thenReturn(user);
            when(orderService.getOrder(1L)).thenReturn(order);

            doThrow(new CoreException(ErrorType.BAD_REQUEST, "포인트 부족"))
                    .when(paymentService).save(any(Payment.class));

            CoreException ex = assertThrows(CoreException.class,
                    () -> paymentApplicationService.createPayment(cmd));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
