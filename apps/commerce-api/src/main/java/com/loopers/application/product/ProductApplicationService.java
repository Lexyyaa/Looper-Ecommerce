package com.loopers.application.product;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.product.*;
import com.loopers.domain.rank.Rank;
import com.loopers.domain.rank.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductApplicationService implements ProductUsecase {

    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final ProductSummaryService productSummaryService;
    private final LikeService likeService;
    private final BrandService brandService;
    private final RankingService rankingService;
    private final ProductEventPublisher productEventPublisher;
    private final ProductActivityPublisher productActivityPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<ProductInfo.Summary> getProductSummaries(ProductCommand.List command) {
        return productSummaryService.getProductSummaries(command).stream()
                .map(ProductInfo.Summary::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductInfo.Detail getProductDetail(String loginId, Long productId) {
        // 상품정보조회
        Product product = productService.getProduct(productId);
        // 상품 옵션별재고 조회
        List<ProductSku> skus = productSkuService.getByProductId(productId);
        // 좋아요 개수 조회
        Long likeCount = likeService.getLikeCount(productId, LikeTargetType.PRODUCT);
        // 옵션 중 최저가 조회
        Long minPrice = productSkuService.getMinPrice(productId);
        // 브랜드 정보 조회
        String brandName = brandService.get(product.getBrandId()).getName();

        // 오늘 기준 랭킹 조회 (없으면 null)
        Rank rank = rankingService.getRankInfo(LocalDate.now(ZoneId.of("Asia/Seoul")), productId,"daily");

        // 사용자 활동로그(상품 조회)
        productActivityPublisher.productDetail(new ProductActivityPayload.ProductDetailViewed(loginId, productId));
        //상품 조회 수 집계 이벤트 발행
        productEventPublisher.productDetail(new ProductEvent.ProductDetailViewed(loginId, productId));

        return ProductInfo.Detail.from(product, brandName, skus, minPrice, likeCount);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "product:detail",
            key = "#productId + ':' + T(java.time.LocalDate).now(T(java.time.ZoneId).of('Asia/Seoul')).toString()",
            unless = "#result == null"
    )
    public ProductInfo.Detail getProductDetailWithCacheable(String loginId, Long productId) {
        // 상품정보
        Product product = productService.getProduct(productId);
        // 옵션/재고
        List<ProductSku> skus = productSkuService.getByProductId(productId);
        // 브랜드명
        String brandName = brandService.get(product.getBrandId()).getName();

        // 오늘 기준 랭킹 조회 (없으면 null)
        Rank rank = rankingService.getRankInfo(LocalDate.now(ZoneId.of("Asia/Seoul")), productId,"daily");

        // 사용자 활동로그(상품 조회)
        productActivityPublisher.productDetail(new ProductActivityPayload.ProductDetailViewed(loginId, productId));
        //상품 조회 수 집계 이벤트 발행
        productEventPublisher.productDetail(new ProductEvent.ProductDetailViewed(loginId, productId));

        // 랭킹 포함 DTO로 반환
        return ProductInfo.Detail.fromProductWithRank(product, brandName, skus, rank);
    }

    @Transactional
    @CacheEvict(value = "product:detail", key = "#command.id")
    public ProductInfo.Item updateProductWithEvictCache(ProductCommand.Update command) {
        // 상품정보조회
        Product product = productService.getProduct(command.id());
        // 옵션 및 재고정보 수정
        List<ProductSku> skus = productSkuService.saveProductSkus(product,command);
        // 최저가 반환
        Long minPrice = productSkuService.getMinPrice(command.id());
        // 저장
        Product saved = productService.saveProduct(product,minPrice);
        return ProductInfo.Item.from(saved,skus);
    }

    @Override
    @Transactional
    public ProductInfo.Item updateProduct(ProductCommand.Update command) {
        // 상품정보조회
        Product product = productService.getProduct(command.id());
        // 옵션 및 재고정보 수정
        List<ProductSku> skus = productSkuService.saveProductSkus(product,command);
        // 최저가 반환
        Long minPrice = productSkuService.getMinPrice(command.id());
        // 저장
        Product saved = productService.saveProduct(product,minPrice);
        return ProductInfo.Item.from(saved,skus);
    }
}



