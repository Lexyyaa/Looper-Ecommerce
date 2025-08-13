package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSummaryService {

    private final ProductSummaryRepository productSummaryRepository;

    public List<ProductSummary> getProductSummaries(ProductCommand.List command) {
        List<ProductSummaryProjection> projections = productSummaryRepository.findProductSummaries(
                command.brandId(),
                command.sortType(),
                command.page(),
                command.size()
        );

        return projections.stream()
                .map(p -> new ProductSummary(
                        p.getId(),
                        p.getName(),
                        p.getMinPrice(),
                        p.getLikeCount(),
                        p.getStatus(),
                        p.getCreatedAt()
                ))
                .toList();
    }
}
