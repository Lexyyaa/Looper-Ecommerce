package com.loopers.domain.product;

import java.util.List;

public interface ProductSummaryRepository {
    List<ProductSummaryProjection> findProductSummaries(Long brandId, ProductSortType sortType,int page, int size);
}
