package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductSkuService {

    private final ProductRepository productRepository;

    public List<ProductSku> getByProductId(Long productId) {
        return productRepository.findAllByProductId(productId);
    }

    public ProductSku getBySkuId(Long skuId) {
        return productRepository.findBySkuId(skuId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,"존재하지 않는 상품 SKUID: " + skuId));
    }

    public void reserveStock(ProductSku sku, int qty) {
        if (sku.avaliableQunatity() < qty) {
            throw new CoreException(ErrorType.BAD_REQUEST,"재고가 부족합니다.");
        }
        sku.reserveStock(qty);
        productRepository.saveProductSku(sku);
    }

    public boolean isAllSoldOut(Long productId) {
        return productRepository.existsAvailableStock(productId);
    }
}
