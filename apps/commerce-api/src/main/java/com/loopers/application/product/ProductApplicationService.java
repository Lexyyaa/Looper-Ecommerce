package com.loopers.application.product;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductApplicationService implements ProductUsecase {

    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final LikeService likeService;
    private final BrandService brandService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductInfo.Summary> getProductSummaries(ProductCommand.List command) {
        return productService.getProductSummaries(command).stream()
                .map(ProductInfo.Summary::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductInfo.Detail getProductDetail(Long productId) {
        // 상품정보조회
        Product product = productService.getProduct(productId);
        // 상품 옵션별재고 조회
        List<ProductSku> skus = productSkuService.getByProductId(productId);
        // 좋아요 개수 조회
        long likeCount = likeService.getLikeCount(productId, LikeTargetType.PRODUCT);
        // 브랜드 정보 조회
        String brandName = brandService.get(product.getBrandId()).getName();

        return ProductInfo.Detail.from(product, brandName, skus, likeCount);
    }

}



