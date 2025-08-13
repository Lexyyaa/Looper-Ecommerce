package com.loopers.interfaces.api.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSku;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.controller.product.ProductV1Response;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@DisplayName("Product E2E Test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
class ProductV1ApiE2ETest {


    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

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

    @BeforeEach
    void setup() {
        databaseCleanUp.truncateAllTables();

        Brand brandA = Brand.create("브랜드A", "브랜드A");
        Brand brandB = Brand.create("브랜드B", "브랜드B");
        brandRepository.save(brandA);
        brandRepository.save(brandB);
        brandAId = brandA.getId();
        brandBId = brandB.getId();

        IntStream.range(0, 20).forEach(i -> {
            User user = userRepository.save(User.create("user" + (i + 1), User.Gender.M, "사용자" + (i + 1), "2020-01-01", "user" + (i + 1) + "@test.com", (long) (1000 * (i + 1))));
            userIds[i] = user.getId();
        });

        for (int i = 0; i < 20; i++) {
            String productName = "상품" + (1 + i);
            Long brandId = (i % 2 == 0) ? brandAId : brandBId;
            Product product = Product.create(productName, Product.Status.ACTIVE, brandId);

            ZonedDateTime createdAt = ZonedDateTime.of(2025, 1, 20 - i, 10, 0, 0, 0, ZoneId.of("Asia/Seoul"));
            ReflectionTestUtils.setField(product, "createdAt", createdAt);

            Product savedProduct = productRepository.saveProduct(product);
            productIds[i] = savedProduct.getId();

            int minPrice = (i + 1) * 1000;
            productRepository.saveProductSku(ProductSku.create(savedProduct, "sku-" + productName + "-1", minPrice, 10, 0));
            productRepository.saveProductSku(ProductSku.create(savedProduct, "sku-" + productName + "-2", minPrice + 500, 5, 0));

//            // 좋아요 수 설정
//            IntStream.range(0, i + 1).forEach(j -> {
//                productRepository.save(Like.create(1L + j, savedProduct.getId(), LikeTargetType.PRODUCT));
//            });
        }

        for (int i = 0; i < 20; i++) {
            Long productId = productIds[19 - i];
            int likesToCreate = i + 1;

            for (int j = 0; j < likesToCreate; j++) {
                likeRepository.save(Like.create(userIds[j], productId, LikeTargetType.PRODUCT));
            }
        }
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @Test
    @DisplayName("[상품 목록] 기본(최근순, page=0,size=5) 페이징 정상")
    void success_list_defaultPaging() {
        ResponseEntity<ApiResponse<ProductV1Response.Summaries>> response = testRestTemplate.exchange(
                "/api/v1/products",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ApiResponse<ProductV1Response.Summaries> body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductV1Response.Summaries summaries = response.getBody().data();
        log.info("summaries : {} ",body.data().summaries());

        assertThat(summaries.summaries()).hasSize(5);
        assertThat(summaries.summaries().get(0).name()).isEqualTo("상품1");
        assertThat(summaries.summaries().get(1).name()).isEqualTo("상품2");
        assertThat(summaries.summaries().get(0).price()).isEqualTo(1000);
    }


    @Test
    @DisplayName("[상품 목록] 브랜드ID로 필터가 적용된다.")
    void success_list_withBrandFilter() {
        String url = UriComponentsBuilder.fromUriString("/api/v1/products")
                .queryParam("brandId", brandAId)
                .build().toUriString();

        ResponseEntity<ApiResponse<ProductV1Response.Summaries>> response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductV1Response.Summaries summaries = response.getBody().data();
        assertThat(summaries.summaries()).hasSize(5);
        assertThat(summaries.summaries().get(0).name()).isEqualTo("상품1");
        assertThat(summaries.summaries().get(1).name()).isEqualTo("상품3");
    }




    @Test
    @DisplayName("[상품 목록] 낮은 가격순으로 정렬된다.")
    void success_list_sortsByLowPrice() {
        String url = UriComponentsBuilder.fromUriString("/api/v1/products")
                .queryParam("sortType", ProductSortType.LOW_PRICE)
                .build().toUriString();

        ResponseEntity<ApiResponse<ProductV1Response.Summaries>> response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductV1Response.Summaries summaries = response.getBody().data();
        assertAll(
                () -> assertThat(summaries.summaries()).hasSize(5),
                () -> assertThat(summaries.summaries().get(0).name()).isEqualTo("상품1"),
                () -> assertThat(summaries.summaries().get(1).name()).isEqualTo("상품2"),
                () -> assertThat(summaries.summaries().get(0).price()).isEqualTo(1000)
        );
    }

    @Test
    @DisplayName("[상품 목록] 좋아요 순으로 정렬된다.")
    void success_list_sortByLike() {
        String url = UriComponentsBuilder.fromUriString("/api/v1/products")
                .queryParam("sortType", ProductSortType.LIKE)
                .build().toUriString();

        ResponseEntity<ApiResponse<ProductV1Response.Summaries>> response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        log.info("response : {}", response.getBody().data());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductV1Response.Summaries summaries = response.getBody().data();

        assertAll(
                () -> assertThat(summaries.summaries()).hasSize(5),
                () -> assertThat(summaries.summaries().get(0).name()).isEqualTo("상품1"),
                () -> assertThat(summaries.summaries().get(1).name()).isEqualTo("상품2"),
                () -> assertThat(summaries.summaries().get(0).likeCount()).isEqualTo(20L)
        );
    }

    @Test
    @DisplayName("[상품 상세] 존재하는 상품이면 정상 응답한다.")
    void success_detail_found() {
        Long existingProductId = productIds[19]; // 상품20의 ID
        log.info("existingProductId : {}", existingProductId);

        ResponseEntity<ApiResponse<ProductV1Response.Detail>> response = testRestTemplate.exchange(
                "/api/v1/products/" + existingProductId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductV1Response.Detail detail = response.getBody().data();
        log.info("response : {}", response.getBody().data());

        assertAll(
                () -> assertThat(detail.name()).isEqualTo("상품20"),
                () -> assertThat(detail.brandName()).isEqualTo("브랜드B"),
                () -> assertThat(detail.likeCount()).isEqualTo(1L),
                () -> assertThat(detail.skus()).hasSize(2)
        );
    }

    @Test
    @DisplayName("[상품 상세] 존재하지 않는 상품이면 404")
    void failure_detail_notFound() {
        Long nonExistentProductId = 9999L;

        ResponseEntity<ApiResponse> response = testRestTemplate.getForEntity("/api/v1/products/" + nonExistentProductId, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().meta().message()).contains("존재하지 않는 상품 ID: "+nonExistentProductId);
    }
}
