package com.loopers.domain.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSku;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("LikeService 통합 테스트")
class LikeServiceIntegrationTest {

    @Autowired
    LikeService likeService;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    Long productId1;
    Long productId2;

    @BeforeEach
    void setUp() {
        Product p1 = Product.create("맥북에어", Product.Status.ACTIVE,1L);
        p1 = productRepository.saveProduct(p1);
        ProductSku p1Sku = ProductSku.create(p1, "macbookair-gray-16",1000, 10, 0);
        productRepository.saveProductSkuAndFlush(p1Sku);
        productId1 = p1.getId();

        Product p2 = Product.create("맥북프로", Product.Status.ACTIVE,1L);
        p2 = productRepository.saveProduct(p2);
        ProductSku p2Sku = ProductSku.create(p2, "macbookpro-gray-16",2000, 10, 0);
        productRepository.saveProductSkuAndFlush(p2Sku);
        productId2 = p2.getId();
    }

    @AfterEach
    void tearDown(){
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("[성공] 좋아요 저장 후 카운트 증가")
    void success_save_and_count() {
        long before = likeService.getLikeCount(productId1, LikeTargetType.PRODUCT);

        likeService.save(100L, productId1, LikeTargetType.PRODUCT);

        long after = likeService.getLikeCount(productId1, LikeTargetType.PRODUCT);
        assertThat(after).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("[실패] 동일 사용자/대상 중복 좋아요 시 제약 위반 예외")
    void failure_save_duplicate_like() {
        likeService.save(101L, productId1, LikeTargetType.PRODUCT);

        assertThrows(DataIntegrityViolationException.class,
                () -> likeService.save(101L, productId1, LikeTargetType.PRODUCT));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 좋아요 삭제 시 BAD_REQUEST")
    void failure_delete_when_not_exists()   {
        CoreException ex = assertThrows(CoreException.class,
                () -> likeService.delete(999L, productId1, LikeTargetType.PRODUCT));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("[성공] 좋아요 삭제 시 카운트 감소")
    void success_delete() {
        likeService.save(102L, productId1, LikeTargetType.PRODUCT);
        assertThat(likeService.getLikeCount(productId1, LikeTargetType.PRODUCT)).isEqualTo(1);

        likeService.delete(102L, productId1, LikeTargetType.PRODUCT);

        assertThat(likeService.getLikeCount(productId1, LikeTargetType.PRODUCT)).isEqualTo(0);
    }

    @Test
    @DisplayName("[성공] 좋아요한 상품 목록 조회(페이지네이션 포함 최소 검증)")
    void success_getLikedProducts() {
        likeService.save(201L, productId1, LikeTargetType.PRODUCT);
        likeService.save(201L, productId2, LikeTargetType.PRODUCT);

        List<LikedProduct> page0 = likeService.getLikedProducts(201L, 0, 10);
        assertThat(page0).isNotEmpty();
        assertThat(page0.stream().map(LikedProduct::id)).contains(productId1, productId2);

        LikedProduct any = page0.get(0);
        assertThat(any.likeCount()).isGreaterThanOrEqualTo(0);
        assertThat(any.status()).isNotNull();

        List<LikedProduct> pageSized = likeService.getLikedProducts(201L, 0, 1);
        assertThat(pageSized).hasSize(1);
    }
}
