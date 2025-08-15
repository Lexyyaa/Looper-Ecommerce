package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product getProduct(Long productId) {
        Product product = productRepository.findBy(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,"존재하지 않는 상품 ID: " + productId));

        if (!product.isAvailable()) {
            throw new CoreException(ErrorType.BAD_REQUEST,"판매중인 상품이 아닙니다.");
        }
        return product;
    }

    @Transactional
    public void updateStatus(boolean isAllSoldOut, Long productId) {
        if (!isAllSoldOut) return;

        Product product = productRepository.findBy(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,"존재하지 않는 상품 ID: " + productId));

        product.changeStatus(Product.Status.SOLD_OUT);
        productRepository.saveProduct(product);
    }

    public Product saveProduct(Product product, Long minPrice) {
        product.updateMinPrice(minPrice);
        return productRepository.saveProduct(product);
    }

    public Product updateLikeCnt(Product product, Long likeCnt) {
        product.updateLikeCnt(likeCnt);
        return productRepository.saveProduct(product);
    }

}
