package com.loopers.application.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PointPaymentProcessor implements PaymentProcessor {

    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Override
    public Payment.Method getMethod() {
        return Payment.Method.POINT;
    }

    @Override
    @Transactional // 별도의 재시도는 없음, DB Timeout 정도만 관리.
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
            // 주문상태변경(주문완료)
            orderService.confirmOrder(order);
            // 결제상태변경(결제완료)
            saved = paymentService.confirmPayment(payment);
        }catch (CoreException e){
            // 결제상태변경(결제실패)
            saved = paymentService.cancelPayment(payment,e.getCustomMessage());
        }

        return saved;
    }
}
