package com.loopers.domain.product;

public interface ProductEventPublisher {
    void productDetail(ProductEvent.ProductDetailViewed event);
}
