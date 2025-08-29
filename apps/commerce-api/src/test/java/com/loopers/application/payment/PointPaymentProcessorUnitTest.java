package com.loopers.application.payment;

import com.loopers.domain.monitoring.resultlog.ResultLogPayload;
import com.loopers.domain.monitoring.resultlog.ResultLogPublisher;
import com.loopers.domain.monitoring.resultlog.payload.PaymentResultLogs;
import com.loopers.domain.order.Order;
import com.loopers.domain.payment.*;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.shared.logging.Envelope;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointPaymentProcessor Unit")
class PointPaymentProcessorUnitTest {

    @Mock
    private UserService userService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentEventPublisher paymentEventPublisher;
    @Mock
    private ResultLogPublisher resultLogPublisher;

    @InjectMocks
    PointPaymentProcessor pointPaymentProcessor;

    @Nested
    @DisplayName("[포인트결제]")
    class Pay {

        // @Test
        @DisplayName("[성공] 포인트 결제 성공 시 이벤트/로그 발행")
        void success_pay_point() {
            User user   = User.builder().id(1L).build();
            Order order  = Order.create(10L,3000L);
            PaymentCommand.CreatePayment cmd =
                    new PaymentCommand.CreatePayment("loginId", 1L, 1000L, Payment.Method.POINT,
                            new PaymentCommand.CardPaymentDetails("삼성", "1234-1234-1234-1234"));
            var pending = Payment.builder()
                    .userId(55L)
                    .orderId(order.getId())
                    .amount(order.getFinalPrice())
                    .method(Payment.Method.POINT)
                    .status(Payment.Status.PENDING).build();

            var confirmed = Payment.builder()
                    .userId(56L)
                    .orderId(order.getId())
                    .amount(order.getFinalPrice())
                    .method(Payment.Method.POINT)
                    .status(Payment.Status.SUCCESS).build();

            when(paymentService.createPending(user.getId(), order.getId(), order.getFinalPrice(), Payment.Method.POINT))
                    .thenReturn(pending);
            when(paymentService.confirmPayment(pending)).thenReturn(confirmed);

            pointPaymentProcessor.pay(user, order, cmd);

            verify(userService).usePoint(user, order.getFinalPrice());
            verify(paymentService).confirmPayment(pending);

            verify(paymentEventPublisher).publish((PaymentEvent.PaymentSucceededEvent) argThat(ev ->
                    ev instanceof PaymentEvent.PaymentSucceededEvent e && e.order() == order));

            verify(resultLogPublisher).publish((Envelope<? extends ResultLogPayload>) argThat((Envelope<?> env) ->
                    "loginId".equals(env.actorId())
                            && env.payload() instanceof PaymentResultLogs.PaymentSucceeded p
                            && p.orderId().equals(order.getId())
                            && p.paymentId().equals(confirmed.getId())
                            && p.amount().equals(order.getFinalPrice())
                            && p.method() == Payment.Method.POINT
            ));
        }

        // @Test
        @DisplayName("[실패] 포인트 사용 도중 예외 → 결제취소/실패 이벤트/로그 발행")
        void failure_pay_point_when_usePoint_throws() {
            User user   = User.builder().id(1L).build();
            Order order  = Order.create(10L,3000L);
            PaymentCommand.CreatePayment cmd =
                    new PaymentCommand.CreatePayment("loginId", 1L, 1000L, Payment.Method.POINT,
                            new PaymentCommand.CardPaymentDetails("삼성", "1234-1234-1234-1234"));
            var pending = Payment.builder()
                    .userId(55L)
                    .orderId(order.getId())
                    .amount(order.getFinalPrice())
                    .method(Payment.Method.POINT)
                    .status(Payment.Status.PENDING).build();

            var cancelled = Payment.builder()
                    .userId(57L)
                    .orderId(order.getId())
                    .amount(order.getFinalPrice())
                    .method(Payment.Method.POINT)
                    .status(Payment.Status.FAILED).build();

            when(paymentService.createPending(user.getId(), order.getId(), order.getFinalPrice(), Payment.Method.POINT))
                    .thenReturn(pending);
            doThrow(new CoreException(ErrorType.BAD_REQUEST,"잔액이 부족합니다."))
                    .when(userService).usePoint(user, order.getFinalPrice());
            when(paymentService.cancelPayment(eq(pending), anyString())).thenReturn(cancelled);

            pointPaymentProcessor.pay(user, order, cmd);

            verify(paymentService).cancelPayment(eq(pending), anyString());

            verify(paymentEventPublisher).publish((PaymentEvent.PaymentSucceededEvent) argThat(ev ->
                    ev instanceof PaymentEvent.PaymentFailedEvent e && e.order() == order));

            verify(resultLogPublisher).publish((Envelope<? extends ResultLogPayload>) argThat((Envelope<?> env) ->
                    "loginId".equals(env.actorId())
                            && env.payload() instanceof PaymentResultLogs.PaymentFailed p
                            && p.orderId().equals(order.getId())
                            && p.paymentId().equals(cancelled.getId())
                            && p.amount().equals(order.getFinalPrice())
                            && p.method() == Payment.Method.POINT
                            && p.reason() != null
            ));
        }
    }
}
