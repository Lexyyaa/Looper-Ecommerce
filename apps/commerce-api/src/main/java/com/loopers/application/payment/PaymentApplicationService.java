package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.paymentgateway.PaymentDetail;
import com.loopers.domain.product.ProductSkuService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentApplicationService implements PaymentUsecase {

    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ProductSkuService productSkuService;

    private final PaymentProcessorFactory paymentProcessorFactory;

    @Override
    public PaymentInfo.CreatePayment createPayment(PaymentCommand.CreatePayment command) {
        //사용자 조회
        User user = userService.getUserWithLock(command.loginId());
        // 결제 가능 상태의 주문 가져오기
        Order order = orderService.getPendingOrder(command.orderId());
        // 결제 방식별 프로세서 선택
        PaymentProcessor processor = paymentProcessorFactory.of(command.method());
        // 결제 진행
        Payment saved = processor.pay(user, order, command);
        return PaymentInfo.CreatePayment.from(saved);
    }

    @Override
    @Transactional
    public void syncPayment(PaymentCommand.SyncPayment command) {

        // 결제내역 조회
        Payment payment = paymentService.findByTxKey(command.transactionKey());

        // 매핑되는 주문 조회
        Order order = orderService.getPendingOrder(payment.getOrderId());

        // orderId 와 txKey를 비교하여 유일한 결제내역을 조회
        PaymentDetail paymentDetail = paymentService.getUniquePaymentDetail(order.getId(), payment.getTxKey());

        // 주문 상태/재고 처리
        switch (paymentDetail.status()) {
            case Payment.Status.SUCCESS -> {
                order.getOrderItems().forEach(item ->
                        productSkuService.confirmStock(item.getProductSkuId(), item.getQuantity())
                );
                orderService.confirmOrder(order);
            }
            case Payment.Status.FAILED -> {
                order.getOrderItems().forEach(item ->
                        productSkuService.rollbackReservedStock(item.getProductSkuId(), item.getQuantity())
                );
                orderService.cancelOrder(order);
            }
        }
    }

    @Override
    public void pollRecentPayments() {
        // 최근 1시간동안 건에 대해서만 폴링 진행
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        List<Payment> candidates = paymentService.getRecentWaiting(since);
        if (candidates.isEmpty()) {
            return;
        }

        for (Payment payment : candidates) {
            this.syncPayment(PaymentCommand.SyncPayment.from(payment));
        }
    }

    @Override
    @Transactional
    public PaymentInfo.CancelPayment cancelPayment(PaymentCommand.CancelPayment command) {
        // 사용자 조회
        User user = userService.getUserWithLock(command.loginId());
        // 결제 조회
        Payment payment = paymentService.getPayment(command.paymentId());
        // 결제 취소
        paymentService.cancelPayment(payment,"");
        // 결제수단별 복구 처리
        if (payment.getMethod() == Payment.Method.POINT) {
            UserCommand.Charge chargeCommand  = new UserCommand.Charge(command.loginId(), payment.getAmount());
            userService.charge(chargeCommand);
        }
        // 취소 가능 주문 조회
        Order order = orderService.getCancelableOrder(command.orderId(),user.getId());
        // 재고 복구
        order.getOrderItems().forEach(item ->
                productSkuService.rollbackReservedStock(item.getProductSkuId(), item.getQuantity())
        );
        // 주문 상태 변경 및 저장
        orderService.cancelOrder(order);
        UserCommand.Charge charge = new UserCommand.Charge(command.loginId(), payment.getAmount());
        userService.charge(charge);
        return PaymentInfo.CancelPayment.from(payment);
    }
}
