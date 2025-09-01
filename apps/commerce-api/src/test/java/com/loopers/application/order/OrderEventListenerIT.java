package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@DisplayName("OrderEventListener IT")
class OrderEventListenerIT {

    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private PlatformTransactionManager txManager;

    @MockBean
    OrderService orderService;

    @Test
    @DisplayName("[성공] 결제성공 이벤트 커밋 후 주문확정 호출")
    void success_confirm_after_commit() {
        var order = Order.builder().userId(10L).build();

        new TransactionTemplate(txManager).execute(status -> {
            publisher.publishEvent(new PaymentEvent.PaymentSucceededEvent(order));
            return null;
        });

        verify(orderService).confirmOrder(order);
    }

    @Test
    @DisplayName("[성공] 결제실패 이벤트 커밋 후 주문취소 호출")
    void success_cancel_after_commit() {
        var order = Order.builder().userId(11L).build();

        new TransactionTemplate(txManager).execute(status -> {
            publisher.publishEvent(new PaymentEvent.PaymentFailedEvent(order));
            return null;
        });

        verify(orderService).cancelOrder(order);
    }

    @Test
    @DisplayName("[성공] 롤백 시 리스너 미호출")
    void success_not_called_on_rollback() {
        var order = Order.builder().userId(12L).build();

        try {
            new TransactionTemplate(txManager).execute(status -> {
                publisher.publishEvent(new PaymentEvent.PaymentSucceededEvent(order));
                status.setRollbackOnly();
                throw new RuntimeException("force rollback");
            });
        } catch (RuntimeException ignore) {}

        verifyNoInteractions(orderService);
    }
}
