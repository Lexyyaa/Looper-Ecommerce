package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductSkuService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentApplicationService implements PaymentUsecase {

    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ProductSkuService productSkuService;
    private final ProductService productService;

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

    @Override
    @Transactional
    public PaymentInfo.CancelPayment cancelPayment(PaymentCommand.CancelPayment command) {
        // 사용자 조회
        User user = userService.getUser(command.loginId());
        // 결제 조회
        Payment payment = paymentService.getPayment(command.paymentId());
        // 취소 가능 여부 검증
        paymentService.validateCancelable(payment,user);
        // 결제수단별 복구 처리
        if (payment.getMethod() == Payment.Method.POINT) {
            UserCommand.Charge chargeCommand  = new UserCommand.Charge(command.loginId(), payment.getAmount());
            userService.charge(chargeCommand);
        }
        // 주문 조회
        Order order = orderService.getOrder(command.orderId());
        // 취소 가능 여부 검증
        orderService.validateCancelable(order,user);
        // 재고 복구
        order.getOrderItems().forEach(item ->
                productSkuService.rollbackReservedStock(item.getProductSkuId(), item.getQuantity())
        );
        // 주문 상태 변경 및 저장
        orderService.cancelOrder(order);
        // 결제 취소
        paymentService.cancelPayment(payment);

        return PaymentInfo.CancelPayment.from(payment);
    }

}
