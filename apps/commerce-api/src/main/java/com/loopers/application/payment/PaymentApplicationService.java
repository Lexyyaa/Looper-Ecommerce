package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentApplicationService implements PaymentUsecase {

    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public PaymentInfo.CreatePayment createPayment(PaymentCommand.CreatePayment command) {
        //사용자 조회
        User user = userService.getUser(command.loginId());
        // 주문 조회 및 상태 확인
        Order order = orderService.getOrder(command.orderId());
        order.isConfirmed();
        //결제 생성
        Payment payment = Payment.create(
                user.getId(),
                order.getId(),
                command.amount(),
                Payment.Method.valueOf(command.method())
        );
        // 저장
        Payment saved = paymentService.save(payment);
        //주문 상태 변경
        order.confirm();
        orderService.saveOrder(order);

        return PaymentInfo.CreatePayment.from(saved);
    }
}
