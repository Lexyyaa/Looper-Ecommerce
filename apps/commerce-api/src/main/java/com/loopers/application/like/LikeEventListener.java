package com.loopers.application.like;

import com.loopers.domain.like.LikeEvent;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.infrastructure.message.ProductProducer;
import com.loopers.domain.product.CatalogMessage;
import com.loopers.shared.event.Envelope;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LikeEventListener {
    private final LikeService likeService;
    private final ProductService productService;
    private final ProductProducer productProducer;

    @Async("applicationEventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdded(Envelope<LikeEvent.Added> e){
        Long likeCnt = likeService.getLikeCount(e.payload().targetId(), e.payload().targetType());
        Product product = productService.getProduct(e.payload().targetId());

        productService.updateLikeCnt(product, likeCnt);

        Envelope<CatalogMessage.LikeChanged> record = Envelope.of(
                e.actorId(),
                new CatalogMessage.LikeChanged(
                        product.getId(),
                        e.payload().targetType().name(),
                        likeCnt
                )
        );
        productProducer.send(String.valueOf(product.getId()),record);
    }

    @Async("applicationEventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRemoved(Envelope<LikeEvent.Removed> e) {
        Long likeCnt = likeService.getLikeCount(e.payload().targetId(), e.payload().targetType());
        Product product = productService.getProduct(e.payload().targetId());
        productService.updateLikeCnt(product, likeCnt);

        Envelope<CatalogMessage.LikeChanged> record = Envelope.of(
                e.actorId(),
                new CatalogMessage.LikeChanged(
                        product.getId(),
                        e.payload().targetType().name(),
                        likeCnt
                )
        );
        productProducer.send(String.valueOf(product.getId()),record);
    }
}
