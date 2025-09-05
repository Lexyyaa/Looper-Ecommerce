package com.loopers.application.product;

import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.payment.PaymentEvent;
import com.loopers.domain.product.*;
import com.loopers.infrastructure.message.ProductProducer;
import com.loopers.shared.event.Envelope;
import com.loopers.infrastructure.message.ProductSkuProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductSkuService productSkuService;
    private final ProductService productService;
    private final ProductProducer productProducer;
    private final ProductSkuProducer productSkuProducer;
    private final CacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onPaymentSuccess(Envelope<PaymentEvent.PaymentSucceededEvent> e) {
        e.payload().order().getOrderItems().forEach(item -> {
            ProductSku confirmed = productSkuService.confirmStock(item.getProductSkuId(), item.getQuantity());

            Envelope<ProductSkuMessage.StockConfirmed> record = Envelope.of(
                    e.actorId(),
                    new ProductSkuMessage.StockConfirmed(
                            confirmed.getProduct().getId(),
                            confirmed.getId(),
                            item.getQuantity()
                    )
            );

            productSkuProducer.send(String.valueOf(confirmed.getId()), record);
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onPaymentFailed(Envelope<PaymentEvent.PaymentFailedEvent>  e) {
        e.payload().order().getOrderItems().forEach(item ->
             productSkuService.rollbackReservedStock(item.getProductSkuId(), item.getQuantity())
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOrderSuccess(OrderEvent.ReCalStock e) {
        e.order().getOrderItems().forEach(item -> {
            ProductSku sku = productSkuService.getBySkuId(item.getProductSkuId());
            boolean isAllSoldOut = productSkuService.isAllSoldOut(sku.getProduct().getId());

            var cache = cacheManager.getCache("product:detail");

            if (cache != null && isAllSoldOut) {
                cache.evict(sku.getProduct().getId());
            }

            productService.updateStatus(isAllSoldOut, sku.getProduct().getId());
        });
    }

    @Async("applicationEventTaskExecutor")
    @EventListener
    public void onDetailViewed(Envelope<ProductActivityPayload.ProductDetailViewed> event) {
        productProducer.send(String.valueOf(event.payload().productId()),event);
    }
}
