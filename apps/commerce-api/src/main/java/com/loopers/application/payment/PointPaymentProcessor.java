package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.payment.*;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.shared.logging.Envelope;
import com.loopers.domain.monitoring.resultlog.ResultLogPublisher;
import com.loopers.domain.monitoring.resultlog.payload.PaymentResultLogs;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PointPaymentProcessor implements PaymentProcessor {

    private final UserService userService;
    private final PaymentService paymentService;
    private final PaymentEventPublisher paymentEventPublisher;
    private final ResultLogPublisher resultLogPublisher;

    @Override
    public Payment.Method getMethod() {
        return Payment.Method.POINT;
    }

    @Override
    @Transactional
    public Payment pay(User user, Order order, PaymentCommand.CreatePayment command) {
        Payment saved = null;

        //결제 생성
        Payment payment = paymentService.createPending(
                user.getId(),
                order.getId(),
                order.getFinalPrice(),
                command.method()
        );

        try{
            // 포인트 사용
            userService.usePoint(user, order.getFinalPrice());
            // 결제상태변경(결제완료)
            saved = paymentService.confirmPayment(payment);
            // 결제 성공시 수행할 이벤트 발행
            paymentEventPublisher.publish(new PaymentEvent.PaymentSucceededEvent(order));
            // 결제 결과 전송
            resultLogPublisher.publish(Envelope.of(
                    command.loginId(),
                    new PaymentResultLogs.PaymentSucceeded(order.getId(), saved.getId(), saved.getAmount(), saved.getMethod())
            ));
        }catch (CoreException e){
            // 결제상태변경(결제실패)
            saved = paymentService.cancelPayment(payment,e.getCustomMessage());
            // 결제 실패시 수행할 이벤트 발행
            paymentEventPublisher.publish(new PaymentEvent.PaymentFailedEvent(order));
            // 결제 결과 전송
            resultLogPublisher.publish(Envelope.of(
                    command.loginId(),
                    new PaymentResultLogs.PaymentFailed(order.getId(), saved.getId(), saved.getAmount(), saved.getMethod(), e.getMessage())
            ));
        }

        return saved;
    }
}
