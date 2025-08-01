package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<ProductSummaryProjection> findProductSummaries(Long brandId, ProductSortType sortType, int page, int size);

    Optional<Product> findBy(Long productId);

    Optional<ProductSku> findBySkuId(Long skuId);

    List<ProductSku> findAllByProductId(Long productId);

    Product saveProduct(Product product);

    ProductSku saveProductSku(ProductSku productSku);

    boolean existsAvailableStock(Long productId);
}
