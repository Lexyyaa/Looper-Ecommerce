package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.product.*;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@SpringBootTest
@DisplayName("ProductApplicationService 통합 테스트")
class ProductApplicaionServiceIntegrationTest {

    @Autowired
    ProductApplicationService productApplicationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    private Long brandAId;
    private Long brandBId;
    private Long[] productIds = new Long[20];
    private Long[] userIds = new Long[20];
    private Long soldOutProductId;
    private Long inactiveProductId;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        databaseCleanUp.truncateAllTables();

        IntStream.range(0, 20).forEach(i -> {
            User user = userRepository.save(User.create("user" + (i + 1), User.Gender.M, "사용자" + (i + 1), "2020-01-01", "user" + (i + 1) + "@test.com", (long) (1000 * (i + 1))));
            userIds[i] = user.getId();
        });

        Brand brandA = brandRepository.save(Brand.create("브랜드A", "브랜드A"));
        Brand brandB = brandRepository.save(Brand.create("브랜드B", "브랜드B"));
        brandAId = brandA.getId();
        brandBId = brandB.getId();

        for (int i = 0; i < 20; i++) {
            String productName = "상품" + (20 - i);
            Long brandId = (i % 2 == 0) ? brandAId : brandBId;
            Product product = Product.create(productName, Product.Status.ACTIVE, brandId);

            ZonedDateTime createdAt = ZonedDateTime.of(2025, 1, 20 - i, 10, 0, 0, 0, ZoneId.of("Asia/Seoul"));
            ReflectionTestUtils.setField(product, "createdAt", createdAt);

            Product savedProduct = productRepository.saveProduct(product);
            productIds[i] = savedProduct.getId();

            int price = (i + 1) * 1000;
            productRepository.saveProductSku(ProductSku.create(savedProduct, "sku-" + productName + "-1", price, 10, 0));
            productRepository.saveProductSku(ProductSku.create(savedProduct, "sku-" + productName + "-2", price + 1000, 5, 0));
            if (i % 2 == 0) {
                productRepository.saveProductSku(ProductSku.create(savedProduct, "sku-" + productName + "-3", price + 2000, 2, 0));
            }
        }

        Product soldOutProduct = productRepository.saveProduct(Product.create("품절상품", Product.Status.SOLD_OUT, brandAId));
        productRepository.saveProductSku(ProductSku.create(soldOutProduct, "sku-soldout", 100000, 0, 0));
        soldOutProductId = soldOutProduct.getId();
        // 품절상품 관련 테케 추가

        Product inactiveProduct = productRepository.saveProduct(Product.create("비활성화상품", Product.Status.INACTIVE, brandBId));
        productRepository.saveProductSku(ProductSku.create(inactiveProduct, "sku-inactive", 200000, 10, 0));
        inactiveProductId = inactiveProduct.getId();
        // 비활성화상품 관련 테케 추가

        for (int i = 0; i < 20; i++) {
            Long productId = productIds[19 - i];
            int likesToCreate = i + 1;

            for (int j = 0; j < likesToCreate; j++) {
                likeRepository.save(Like.create(userIds[j], productId, LikeTargetType.PRODUCT));
            }
        }

        // 좋아요 수 설정 후 Product 엔티티의 likeCnt 필드를 업데이트
//        for (int i = 0; i < 20; i++) {
//            Long productId = productIds[19 - i];
//            Product product = productRepository.findBy(productId).orElseThrow();
//            int likeCount = i + 1;
//            product.updateLikeCnt((long) likeCount);
//            productRepository.saveProduct(product);
//        }
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("[상품 목록 조회]")
    class GetProductSummaries {

        @Test
        @DisplayName("[성공] 첫 페이지(page=0, size=10) 최신순 조회")
        void success_firstPage_latestSort() {
            // arrange
            ProductCommand.List command = new ProductCommand.List(0, 10, null, ProductSortType.RECENT);
            List<String> expectedNames = IntStream.rangeClosed(11, 20)
                    .mapToObj(i -> "상품" + i)
                    .sorted((a, b) -> Integer.compare(Integer.parseInt(b.substring(2)), Integer.parseInt(a.substring(2))))
                    .toList();

            // act
            List<ProductInfo.Summary> result = productApplicationService.getProductSummaries(command);
            log.info("result : {} ",result);

            // assert
            assertAll(
                    () -> assertThat(result).hasSize(10),
                    () -> assertThat(result.stream().map(ProductInfo.Summary::name).toList()).isEqualTo(expectedNames)
            );
        }

        @Test
        @DisplayName("[성공] 두 번째 페이지(page=1, size=10) 최신순 조회")
        void success_secondPage_latestSort() {
            // arrange
            ProductCommand.List command = new ProductCommand.List(1, 10, null, ProductSortType.RECENT);
            List<String> expectedNames = IntStream.rangeClosed(1, 10)
                    .mapToObj(i -> "상품" + i)
                    .sorted((a, b) -> Integer.compare(Integer.parseInt(b.substring(2)), Integer.parseInt(a.substring(2))))
                    .toList();

            // act
            List<ProductInfo.Summary> result = productApplicationService.getProductSummaries(command);
            log.info("result : {} ",result);

            // assert
            assertAll(
                    () -> assertThat(result).hasSize(10),
                    () -> assertThat(result.stream().map(ProductInfo.Summary::name).toList()).isEqualTo(expectedNames)
            );
        }

        @ParameterizedTest
        @MethodSource("sortTypeAndExpectedOrderProvider")
        @DisplayName("[성공] 정렬 조건별 첫 페이지 조회")
        void success_sorts_firstPage(ProductSortType sortType, List<String> expectedNames) {
            // arrange
            ProductCommand.List command = new ProductCommand.List(0, 5, null, sortType);

            // act
            List<ProductInfo.Summary> result = productApplicationService.getProductSummaries(command);
            log.info("result : {} ",result);

//            [Summary[id=1, name=상품20, price=1000, likeCount=20, status=ACTIVE, createdAt=2025-01-20T10:00],
//            Summary[id=2, name=상품19, price=2000, likeCount=19, status=ACTIVE, createdAt=2025-01-19T10:00],
//            Summary[id=3, name=상품18, price=3000, likeCount=18, status=ACTIVE, createdAt=2025-01-18T10:00],
//            Summary[id=4, name=상품17, price=4000, likeCount=17, status=ACTIVE, createdAt=2025-01-17T10:00],
//            Summary[id=5, name=상품16, price=5000, likeCount=16, status=ACTIVE, createdAt=2025-01-16T10:00]]

            // assert
            assertAll(
                    () -> assertThat(result).hasSize(5),
                    () -> assertThat(result.stream().map(ProductInfo.Summary::name).toList()).isEqualTo(expectedNames)
            );
        }

        static Stream<Arguments> sortTypeAndExpectedOrderProvider() {
            return Stream.of(
                    Arguments.of(ProductSortType.RECENT, List.of("상품20", "상품19", "상품18", "상품17", "상품16")),
                    Arguments.of(ProductSortType.LOW_PRICE, List.of("상품20", "상품19", "상품18", "상품17", "상품16")),
                    Arguments.of(ProductSortType.LIKE, List.of("상품20", "상품19", "상품18", "상품17", "상품16"))
            );
        }

        @Test
        @DisplayName("[성공] 특정 브랜드 ID로 필터링 조회 (브랜드A)")
        void success_filterByBrandId() {
            // arrange
            ProductCommand.List command = new ProductCommand.List(0, 10, brandAId, ProductSortType.RECENT);
            List<String> expectedNames = List.of("상품20", "상품18", "상품16", "상품14", "상품12", "상품10", "상품8", "상품6", "상품4", "상품2");

            // act
            List<ProductInfo.Summary> result = productApplicationService.getProductSummaries(command);
            log.info("result : {} ",result);

            // assert
            assertAll(
                    () -> assertThat(result).hasSize(10),
                    () -> assertThat(result.stream().map(ProductInfo.Summary::name).toList()).isEqualTo(expectedNames)
            );
        }
    }

    @Nested
    @DisplayName("[상품 상세 정보 조회]")
    class GetProductDetail {

        @Test
        @DisplayName("[성공] 상품 상세 정보 조회")
        void success_getProductDetail() {
            // arrange
            Long targetProductId = productIds[19];

            // act
            ProductInfo.Detail productDetail = productApplicationService.getProductDetail(targetProductId);

            log.info("productDetail : {} ", productDetail);

            // assert
            assertAll(
                    () -> assertThat(productDetail.name()).isEqualTo("상품1"),
                    () -> assertThat(productDetail.brandName()).isEqualTo("브랜드B"),
                    () -> assertThat(productDetail.likeCount()).isEqualTo(1),
                    () -> assertThat(productDetail.skus()).hasSize(2),
                    () -> assertThat(productDetail.skus().get(0).price()).isEqualTo(20000)
            );
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 상품 ID로 상세 조회 시 예외 발생")
        void failure_getProductDetail_nonexistentProduct() {
            // act & assert
            CoreException ex = assertThrows(CoreException.class,
                    () -> productApplicationService.getProductDetail(9999L));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
