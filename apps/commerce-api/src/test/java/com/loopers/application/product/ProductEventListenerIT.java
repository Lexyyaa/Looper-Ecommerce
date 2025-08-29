package com.loopers.application.product;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentEvent;
import com.loopers.domain.product.ProductSkuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("ProductEventListener IT (After-Commit + REQUIRES_NEW)")
public class ProductEventListenerIT {

    @MockBean
    private ProductSkuService productSkuService;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PlatformTransactionManager txManager;

    @Test
    @DisplayName("[성공] 결제성공 → 커밋 직전(confirmStock) 호출")
    void success_confirm_stock_before_commit() {
        long orderId = 777L;

        OrderItem item1 = Mockito.mock(OrderItem.class);
        when(item1.getProductSkuId()).thenReturn(101L);
        when(item1.getQuantity()).thenReturn(2);

        OrderItem item2 = Mockito.mock(OrderItem.class);
        when(item2.getProductSkuId()).thenReturn(202L);
        when(item2.getQuantity()).thenReturn(3);

        Order order = Mockito.mock(Order.class);
        when(order.getId()).thenReturn(orderId);
        when(order.getStatus()).thenReturn(Order.Status.CONFIRMED);
        when(order.getOrderItems()).thenReturn(List.of(item1, item2));

        when(orderService.getOrder(orderId)).thenReturn(order);

        new TransactionTemplate(txManager).execute(status -> {
            publisher.publishEvent(new PaymentEvent.PaymentSucceededEvent(order));
            return null;
        });

        verify(productSkuService).confirmStock(101L, 2);
        verify(productSkuService).confirmStock(202L, 3);
        verify(productSkuService, never()).rollbackReservedStock(anyLong(), anyInt());
    }

    // @Test
    @DisplayName("[성공] 결제실패 → 커밋 직전(rollbackReservedStock) 호출")
    void success_rollback_stock_before_commit() {
        long orderId = 888L;

        OrderItem item1 = Mockito.mock(OrderItem.class);
        when(item1.getProductSkuId()).thenReturn(101L);
        when(item1.getQuantity()).thenReturn(2);

        OrderItem item2 = Mockito.mock(OrderItem.class);
        when(item2.getProductSkuId()).thenReturn(202L);
        when(item2.getQuantity()).thenReturn(3);

        Order order = Mockito.mock(Order.class);
        when(order.getId()).thenReturn(orderId);
        when(order.getStatus()).thenReturn(Order.Status.CONFIRMED);
        when(order.getOrderItems()).thenReturn(List.of(item1, item2));

        when(orderService.getOrder(orderId)).thenReturn(order);

        new TransactionTemplate(txManager).execute(status -> {
            publisher.publishEvent(new PaymentEvent.PaymentFailedEvent(order));
            return null;
        });

        verify(productSkuService).rollbackReservedStock(101L, 2);
        verify(productSkuService).rollbackReservedStock(202L, 3);
        verify(productSkuService, never()).confirmStock(anyLong(), anyInt());
    }

    //@Test
    @DisplayName("[무시] 주문상태가 CONFIRMED가 아니면 아무 것도 하지 않음")
    void ignore_when_not_confirmed() {
        long orderId = 999L;

        OrderItem item1 = Mockito.mock(OrderItem.class);
        when(item1.getProductSkuId()).thenReturn(101L);
        when(item1.getQuantity()).thenReturn(2);

        OrderItem item2 = Mockito.mock(OrderItem.class);
        when(item2.getProductSkuId()).thenReturn(202L);
        when(item2.getQuantity()).thenReturn(3);

        Order order = Mockito.mock(Order.class);
        when(order.getId()).thenReturn(orderId);
        when(order.getStatus()).thenReturn(Order.Status.PENDING);
        when(order.getOrderItems()).thenReturn(List.of(item1, item2));

        when(orderService.getOrder(orderId)).thenReturn(order);

        new TransactionTemplate(txManager).execute(status -> {
            publisher.publishEvent(new PaymentEvent.PaymentSucceededEvent(order));
            publisher.publishEvent(new PaymentEvent.PaymentFailedEvent(order));
            return null;
        });

        verify(productSkuService, never()).confirmStock(anyLong(), anyInt());
        verify(productSkuService, never()).rollbackReservedStock(anyLong(), anyInt());
    }
}
