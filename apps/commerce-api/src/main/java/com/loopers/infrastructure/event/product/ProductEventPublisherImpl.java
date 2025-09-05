package com.loopers.infrastructure.event.product;

import com.loopers.domain.product.ProductEvent;
import com.loopers.domain.product.ProductEventPublisher;
import com.loopers.shared.event.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisherImpl implements ProductEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void productDetail(ProductEvent.ProductDetailViewed event) {
        applicationEventPublisher.publishEvent(Envelope.of(event.loginId(),event));
    }
}
