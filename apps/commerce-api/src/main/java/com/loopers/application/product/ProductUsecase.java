package com.loopers.application.product;

import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductInfo;

import java.util.List;

public interface ProductUsecase {
    List<ProductInfo.Summary> getProductSummaries(ProductCommand.List command);
    ProductInfo.Detail getProductDetail(Long productId);
    ProductInfo.Item updateProduct(ProductCommand.Update command);
    ProductInfo.Detail getProductDetailWithCacheable(Long productId);
}


