package com.loopers.application.product;

import com.loopers.domain.product.ProductActivityPayload;
import com.loopers.infrastructure.message.ActivityProducer;
import com.loopers.shared.event.Envelope;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductActivityListener {

    private final ActivityProducer activityProducer;

    @Async("applicationEventTaskExecutor")
    @EventListener
    public void onDetailViewed(Envelope<ProductActivityPayload.ProductDetailViewed> event) {
        activityProducer.send(event);
    }
}
