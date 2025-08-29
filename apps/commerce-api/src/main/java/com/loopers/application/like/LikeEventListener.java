package com.loopers.application.like;

import com.loopers.domain.like.LikeEvent;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
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

    @Async("applicationEventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(LikeEvent.Added event){
        Long cnt = likeService.getLikeCount(event.targetId(), event.targetType());
        Product product = productService.getProduct(event.targetId());
        productService.updateLikeCnt(product, cnt);
    }

    @Async("applicationEventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(LikeEvent.Removed event) {
        Long cnt = likeService.getLikeCount(event.targetId(), event.targetType());
        Product product = productService.getProduct(event.targetId());
        productService.updateLikeCnt(product, cnt);
    }
}
