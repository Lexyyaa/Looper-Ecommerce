package com.loopers.domain.product;

public interface ProductActivityPublisher {
    void productDetail(ProductActivityPayload.ProductDetailViewed event);
}
