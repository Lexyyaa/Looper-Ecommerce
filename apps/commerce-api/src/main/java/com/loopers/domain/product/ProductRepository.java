package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Optional<Product> findBy(Long productId);

    Optional<ProductSku> findBySkuId(Long skuId);

    List<ProductSku> findAllByProductId(Long productId);

    Product saveProduct(Product product);

    ProductSku saveProductSku(ProductSku productSku);
    ProductSku saveProductSkuAndFlush(ProductSku productSku);

    boolean existsAvailableStock(Long productId);

    Optional<ProductSku> findByIdWithOptimisticLock(Long skuId);
}
