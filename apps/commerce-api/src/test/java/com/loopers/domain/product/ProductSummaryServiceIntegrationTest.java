package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@SpringBootTest
@DisplayName("ProductSummaryService 통합 테스트")
public class ProductSummaryServiceIntegrationTest {

    @Autowired
    ProductSummaryService productSummaryService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductRepository productSkuRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    private Long productAId;
    private Long productBId;
    private Long productCId;
    private Long productDId;

    private Long brandAId;
    private Long brandBId;

    private Long user1Id;
    private Long user2Id;
    private Long user3Id;
    private Long user4Id;

    @BeforeEach
    void setUp() throws Exception {
        databaseCleanUp.truncateAllTables();

        User user1 = userRepository.save(User.create("user1", User.Gender.M, "사용자1", "2020-02-21", "user1@yy.zz", 1000L));
        User user2 = userRepository.save(User.create("user2", User.Gender.M, "사용자2", "2020-02-22", "user2@yy.zz", 2000L));
        User user3 = userRepository.save(User.create("user3", User.Gender.M, "사용자3", "2020-02-23", "user3@yy.zz", 3000L));
        User user4 = userRepository.save(User.create("user4", User.Gender.M, "사용자4", "2020-02-24", "user4@yy.zz", 4000L));

        user1Id = user1.getId();
        user2Id = user2.getId();
        user3Id = user3.getId();
        user4Id = user4.getId();

        Brand brandA = brandRepository.save(Brand.create("애플", "Think Different"));
        Brand brandB = brandRepository.save(Brand.create("삼성", "같이의 가치"));
        brandAId = brandA.getId();
        brandBId = brandB.getId();

        // 테스트 데이터 생성: 상품 4개
        // 생성일자 'A > B > C > D' 순서로 설정하기 위해 리플렉션 사용 (ZonedDateTime으로 변경)
        Product productA = Product.create("상품A", Product.Status.ACTIVE, brandAId);
        ReflectionTestUtils.setField(productA, "createdAt", ZonedDateTime.of(2025, 1, 4, 10, 0, 0, 0, ZoneId.of("Asia/Seoul")));
        productAId = productRepository.saveProduct(productA).getId();

        Product productB = Product.create("상품B", Product.Status.ACTIVE, brandBId);
        ReflectionTestUtils.setField(productA, "createdAt", ZonedDateTime.of(2025, 1, 3, 10, 0, 0, 0, ZoneId.of("Asia/Seoul")));
        productBId = productRepository.saveProduct(productB).getId();

        Product productC = Product.create("상품C", Product.Status.ACTIVE, brandAId);
        ReflectionTestUtils.setField(productA, "createdAt", ZonedDateTime.of(2025, 1, 2, 10, 0, 0, 0, ZoneId.of("Asia/Seoul")));
        productCId = productRepository.saveProduct(productC).getId();

        Product productD = Product.create("상품D", Product.Status.ACTIVE, brandBId);
        ReflectionTestUtils.setField(productA, "createdAt", ZonedDateTime.of(2025, 1, 1, 10, 0, 0, 0, ZoneId.of("Asia/Seoul")));
        productDId = productRepository.saveProduct(productD).getId();

        // 상품 SKU 데이터 설정 (minPrice 정렬을 위함)
        // 최소 가격: D(10000) < C(20000) < B(30000) < A(40000) 순
        productSkuRepository.saveProductSku(ProductSku.create(productA, "sku-a1", 40000, 10, 0));
        productSkuRepository.saveProductSku(ProductSku.create(productB, "sku-b1", 30000, 10, 0));
        productSkuRepository.saveProductSku(ProductSku.create(productC, "sku-c1", 20000, 10, 0));
        productSkuRepository.saveProductSku(ProductSku.create(productD, "sku-d1", 10000, 10, 0));
        productSkuRepository.saveProductSku(ProductSku.create(productD, "sku-d2", 15000, 10, 0));

        // 상품 좋아요 데이터 설정 (LIKE 정렬을 위함)
        // 좋아요 수: D(4) > C(3) > A(2) > B(1) 순
        likeRepository.save(Like.create(user1Id, productBId, LikeTargetType.PRODUCT));

        likeRepository.save(Like.create(user1Id, productAId, LikeTargetType.PRODUCT));
        likeRepository.save(Like.create(user2Id, productAId, LikeTargetType.PRODUCT));

        likeRepository.save(Like.create(user1Id, productCId, LikeTargetType.PRODUCT));
        likeRepository.save(Like.create(user2Id, productCId, LikeTargetType.PRODUCT));
        likeRepository.save(Like.create(user3Id, productCId, LikeTargetType.PRODUCT));

        likeRepository.save(Like.create(user1Id, productDId, LikeTargetType.PRODUCT));
        likeRepository.save(Like.create(user2Id, productDId, LikeTargetType.PRODUCT));
        likeRepository.save(Like.create(user3Id, productDId, LikeTargetType.PRODUCT));
        likeRepository.save(Like.create(user4Id, productDId, LikeTargetType.PRODUCT));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("[상품 목록 조회]")
    class GetProductSummaries {

        @Test
        @DisplayName("[성공] 첫 페이지(page=0, size=4) 최신순 조회")
        void success_firstPage_latestSort() {
            // arrange
            ProductCommand.List command = new ProductCommand.List(0, 4, brandAId,ProductSortType.RECENT);

            // act
            List<ProductSummary> result = productSummaryService.getProductSummaries(command);

            // assert
            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result.get(0).name()).isEqualTo("상품C"),
                    () -> assertThat(result.get(1).name()).isEqualTo("상품A")
            );
        }

        @Test
        @DisplayName("[성공] 두 번째 페이지(page=1, size=2) 최신순 조회")
        void success_secondPage_latestSort() {
            // arrange
            ProductCommand.List command = new ProductCommand.List(0, 4, brandAId,ProductSortType.RECENT);

            // act
            List<ProductSummary> result = productSummaryService.getProductSummaries(command);

            log.info("result  : {} ",result);

            // assert
            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result.get(0).name()).isEqualTo("상품C"),
                    () -> assertThat(result.get(1).name()).isEqualTo("상품A")
            );
        }

        @ParameterizedTest
        @CsvSource(value = {
                "RECENT, 상품D , 상품C, 상품B , 상품A ",
                "LOW_PRICE, 상품D, 상품C, 상품B, 상품A",
                "LIKE, 상품D, 상품C, 상품A, 상품B",
        })
        @DisplayName("[성공] 정렬 조건별 전체 상품 목록 조회")
        void success_allProducts_sortedByCondition(String sortTypeStr, String first, String second, String third, String fourth) {
            // arrange
            ProductSortType sortType = ProductSortType.valueOf(sortTypeStr);
            ProductCommand.List command = new ProductCommand.List(0, 4, null, sortType);

            // act
            List<ProductSummary> result = productSummaryService.getProductSummaries(command);
            log.info("result  : {} ",result);

            // assert
            assertAll(
                    () -> assertThat(result).hasSize(4),
                    () -> assertThat(result.get(0).name()).isEqualTo(first),
                    () -> assertThat(result.get(1).name()).isEqualTo(second),
                    () -> assertThat(result.get(2).name()).isEqualTo(third),
                    () -> assertThat(result.get(3).name()).isEqualTo(fourth)
            );
        }

        @Test
        @DisplayName("[경계값] size=0 이면 빈 목록 반환")
        void boundary_zeroSize() {
            // arrange
            ProductCommand.List command = new ProductCommand.List(0, 0, null, ProductSortType.RECENT);

            // act
            List<ProductSummary> result = productSummaryService.getProductSummaries(command);

            // assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[경계값] 존재하지 않는 브랜드 ID로 조회 시 빈 목록 반환")
        void boundary_nonexistentBrand() {
            // arrange
            ProductCommand.List command = new ProductCommand.List(0, 0, 999L, ProductSortType.RECENT);

            // act
            List<ProductSummary> result = productSummaryService.getProductSummaries(command);

            // assert
            assertThat(result).isEmpty();
        }
    }
}
