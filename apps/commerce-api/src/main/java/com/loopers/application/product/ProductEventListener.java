package com.loopers.application.product;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentEvent;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductSku;
import com.loopers.domain.product.ProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductSkuService productSkuService;
    private final ProductService productService;
    private final OrderService orderService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onPaymentSuccess(PaymentEvent.PaymentSucceededEvent event) {
        var order = orderService.getOrder(event.order().getId());
        if (order.getStatus() != Order.Status.CONFIRMED) return;

        order.getOrderItems().forEach(item ->
                productSkuService.confirmStock(item.getProductSkuId(), item.getQuantity())
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onPaymentFailed(PaymentEvent.PaymentFailedEvent  event) {
        var order = orderService.getOrder(event.order().getId());
        if (order.getStatus() != Order.Status.CONFIRMED) return;
        order.getOrderItems().forEach(item ->
                productSkuService.rollbackReservedStock(item.getProductSkuId(), item.getQuantity())
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOrderSuccess(OrderEvent.ReCalStock e) {
        e.order().getOrderItems().forEach(item -> {
            ProductSku sku = productSkuService.getBySkuId(item.getProductSkuId());
            boolean isAllSoldOut = productSkuService.isAllSoldOut(sku.getProduct().getId());
            productService.updateStatus(isAllSoldOut, sku.getProduct().getId());
        });
    }
}
