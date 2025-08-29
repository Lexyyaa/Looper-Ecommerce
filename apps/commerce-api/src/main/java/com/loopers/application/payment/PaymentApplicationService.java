package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.domain.paymentgateway.PaymentDetail;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.shared.logging.Envelope;
import com.loopers.shared.logging.SystemActors;
import com.loopers.domain.monitoring.activity.ActivityPublisher;
import com.loopers.domain.monitoring.activity.payload.PaymentActivityPayload;
import com.loopers.domain.monitoring.resultlog.ResultLogPublisher;
import com.loopers.domain.monitoring.resultlog.payload.PaymentResultLogs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentApplicationService implements PaymentUsecase {

    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final PaymentEventPublisher paymentEventPublisher;
    private final ActivityPublisher activityPublisher;
    private final ResultLogPublisher resultLogPublisher;

    @Override
    public PaymentInfo.CreatePayment createPayment(PaymentCommand.CreatePayment command) {
        // 사용자활동로그(결제요청)
        activityPublisher.publish(Envelope.of(
                command.loginId(),
                new PaymentActivityPayload.PayRequested(command.orderId(), command.amount(), command.method())
        ));

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
                // 결제 성공시 수행할 이벤트 발행
                paymentEventPublisher.publish(new PaymentEvent.PaymentSucceededEvent(order));
                // 결제 결과 전송
                resultLogPublisher.publish(Envelope.of(
                        SystemActors.PG_CALLBACK,
                        new PaymentResultLogs.PaymentSucceeded(order.getId(), payment.getId(), payment.getAmount(), payment.getMethod())
                ));
            }
            case Payment.Status.FAILED -> {
                paymentEventPublisher.publish(new PaymentEvent.PaymentFailedEvent(order));
                // 결제 결과 전송
                resultLogPublisher.publish(Envelope.of(
                        SystemActors.PG_CALLBACK,
                        new PaymentResultLogs.PaymentFailed(order.getId(), payment.getId(), payment.getAmount(), payment.getMethod(),paymentDetail.reason())
                ));
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
}
