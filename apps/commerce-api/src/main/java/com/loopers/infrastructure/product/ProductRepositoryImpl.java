package com.loopers.infrastructure.product;

import com.loopers.domain.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final ProductSkuJpaRepository productSkuJpaRepository;


    @Override
    public Optional<Product> findBy(Long productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public Optional<ProductSku> findBySkuId(Long skuId) {
        return productSkuJpaRepository.findById(skuId);
    }

    @Override
    public List<ProductSku> findAllByProductId(Long productId) {
        return productSkuJpaRepository.findAllByProductId(productId);
    }

    @Override
    public Product saveProduct(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public ProductSku saveProductSku(ProductSku productSku) {
        return productSkuJpaRepository.save(productSku);
    }

    @Override
    public ProductSku saveProductSkuAndFlush(ProductSku productSku) {
        ProductSku savedProductSku = productSkuJpaRepository.save(productSku);
        productSkuJpaRepository.flush();
        return savedProductSku;
    }

    @Override
    public boolean existsAvailableStock(Long productId) {
        return productSkuJpaRepository.existsAvailableStock(productId);
    }

    @Override
    public Optional<ProductSku> findByIdWithOptimisticLock(Long skuId) {
        return productSkuJpaRepository.findByIdWithOptimisticLock(skuId);
    }

    @Override
    public Long getMinPrice(Long productId) {
        return productSkuJpaRepository.findMinPriceByProductId(productId);
    }

    @Override
    public List<ProductSku> saveAllProductSku(List<ProductSku> productSkus) {
        return productSkuJpaRepository.saveAll(productSkus);
    }
}
