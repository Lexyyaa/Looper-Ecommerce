package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductSummary> getProductSummaries(ProductCommand.List command) {
        List<ProductSummaryProjection> projections = productRepository.findProductSummaries(
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

    public Product getProduct(Long productId) {
        Product product = productRepository.findBy(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,"존재하지 않는 상품 ID: " + productId));

        if (!product.isAvailable()) {
            throw new CoreException(ErrorType.BAD_REQUEST,"판매중인 상품이 아닙니다.");
        }
        return product;
    }

    public void updateStatus(boolean isAllSoldOut, Long productId) {
        if (!isAllSoldOut) return;

        Product product = productRepository.findBy(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,"존재하지 않는 상품 ID: " + productId));

        product.changeStatus(Product.Status.SOLD_OUT);
        productRepository.saveProduct(product);
    }
}
