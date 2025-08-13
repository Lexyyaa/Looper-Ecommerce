package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.product.ProductSummaryProjection;
import com.loopers.domain.product.ProductSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductSummaryRepositoryImpl implements ProductSummaryRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public List<ProductSummaryProjection> findProductSummaries(Long brandId, ProductSortType sortType, int page, int size) {
        return productJpaRepository.findProductSummaries(
                brandId,
                sortType.name(),
                size,
                page * size
        );
    }
}
