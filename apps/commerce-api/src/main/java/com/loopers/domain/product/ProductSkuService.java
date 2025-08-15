package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    public ProductSku getBySkuIdWithLock(Long skuId) {
        return productRepository.findByIdWithOptimisticLock(skuId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,"존재하지 않는 상품 SKUID: " + skuId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProductSku reserveStock(Long skuId, int qty) {
        try {
            ProductSku sku = productRepository.findByIdWithOptimisticLock(skuId)
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품 SKUID: " + skuId));
            sku.reserveStock(qty);
            return productRepository.saveProductSku(sku);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new CoreException(ErrorType.CONFLICT, "동시에 다른 주문이 재고를 선점했습니다. 다시 시도해 주세요.");
        }
    }

    public void rollbackReservedStock(Long skuId, int quantity) {
        ProductSku sku = getBySkuId(skuId);
        sku.rollbackStock(quantity);
        productRepository.saveProductSku(sku);
    }

    public boolean isAllSoldOut(Long productId) {
        return !productRepository.existsAvailableStock(productId);
    }

    public Long getMinPrice(Long productId) {
        return productRepository.getMinPrice(productId);
    }

    public List<ProductSku> saveProductSkus(Product product, ProductCommand.Update command) {
        List<ProductSku> newSkus = command.to(product);

        List<ProductSku> savedSkus = productRepository.saveAllProductSku(newSkus);
        return savedSkus;
    }
}
