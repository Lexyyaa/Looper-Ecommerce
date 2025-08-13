package com.loopers.interfaces.api.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.controller.like.LikeV1Response;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@DisplayName("Like E2E Test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LikeV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
     DatabaseCleanUp databaseCleanUp;

    private User testUser;
    private User otherUser;
    private Product testProduct1;
    private Product testProduct2;
    private Brand brand;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();

        testUser = User.create("test-user-id", User.Gender.M, "test-user-id","2020-02-20","xx1@yy.zz",1000L);
        otherUser = User.create("other-user-id", User.Gender.M, "other-user-nickname","2020-02-21","xx2@yy.zz",1000L);

        userRepository.save(testUser);
        userRepository.save(otherUser);

        brand = Brand.create("test-brand", "test-brand");
        brandRepository.save(brand);

        testProduct1 = Product.create("테스트 상품1", Product.Status.ACTIVE, brand.getId());
        testProduct2 = Product.create("테스트 상품2", Product.Status.ACTIVE, brand.getId());
        productRepository.saveProduct(testProduct1);
        productRepository.saveProduct(testProduct2);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("[좋아요] /api/v1/likes/products/{productId}")
    @Nested
    class LikeProduct {
        private final String BASE_URL = "/api/v1/likes/products/";

        @DisplayName("[성공] 존재하는 유저가 존재하는 상품에 좋아요를 누르면 성공한다.")
        @Test
        void success_whenUserAndProductExist() {
            String url = BASE_URL + testProduct1.getId();
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", testUser.getLoginId());
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<Void>> response = testRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );
            log.info("response : {} ", response.getBody().data());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("[실패] 존재하는 유저가 존재하지 않는 상품에 좋아요를 누르면 404 Not Found를 반환한다.")
        @Test
        void failure_whenProductDoesNotExist() {
            Long notExistsPoductId = 9999L;
            String url = BASE_URL + 9999L;
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", testUser.getLoginId());
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<Void>> response = testRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );
            log.info("response : {} ", response.getBody().data());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().meta().message()).contains("존재하지 않는 상품 ID: "+notExistsPoductId);
        }
    }

    @DisplayName("[좋아요 취소] /api/v1/likes/products/{productId}")
    @Nested
    class UnlikeProduct {
        private final String BASE_URL = "/api/v1/likes/products/";

        @BeforeEach
        void setup() {
            Like like = Like.create(testUser.getId(), testProduct1.getId(), LikeTargetType.PRODUCT);
            likeRepository.save(like);
        }

        @DisplayName("[성공] 좋아요를 누른 유저가 좋아요를 취소하면 성공한다.")
        @Test
        void success_whenUserLikedProduct() {
            String url = BASE_URL + testProduct1.getId();
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", testUser.getLoginId());
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<Void>> response = testRestTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );
            log.info("response : {} ", response.getBody().data());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("[실패] 좋아요를 누르지 않은 유저가 좋아요 취소를 시도하면 404 Not Found를 반환한다.")
        @Test
        void failure_whenUserDidNotLikeProduct() {
            String url = BASE_URL + testProduct2.getId();
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", testUser.getLoginId());
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<Void>> response = testRestTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );
            log.info("response : {} ", response.getBody().data());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().meta().message()).contains("해당 좋아요가 존재하지 않습니다.");
        }
    }

    @DisplayName("[좋아요한 상품 목록 조회] /{userId}/likes/products")
    @Nested
    class GetLikedProducts {

        @BeforeEach
        void setup() {
            Like like1 = Like.create(testUser.getId(), testProduct1.getId(), LikeTargetType.PRODUCT);
            Like like2 = Like.create(testUser.getId(), testProduct2.getId(), LikeTargetType.PRODUCT);
            likeRepository.save(like1);
            likeRepository.save(like2);

            Like like3 = Like.create(otherUser.getId(), testProduct1.getId(), LikeTargetType.PRODUCT);
            likeRepository.save(like3);
        }

        @DisplayName("좋아요한 상품이 있는 유저가 조회하면 목록을 반환한다.")
        @Test
        void success_whenUserHasLikedProducts() {
            String url = "/api/v1/likes/" + testUser.getLoginId() + "/products";
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", testUser.getLoginId());
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<LikeV1Response.LikedProductList>> response = testRestTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            LikeV1Response.LikedProductList result = response.getBody().data();
            assertThat(result.items()).hasSize(2);
            assertAll(
                    () -> assertThat(result.items().get(0).id()).isEqualTo(testProduct2.getId()),
                    () -> assertThat(result.items().get(1).id()).isEqualTo(testProduct1.getId())
            );
        }

        @DisplayName("[성공] 좋아요한 상품이 없는 유저가 조회하면 빈 목록을 반환한다.")
        @Test
        void success_whenUserHasNoLikedProducts() {
            String url = "/api/v1/likes/" + otherUser.getLoginId() + "/products";
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", otherUser.getLoginId());
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<LikeV1Response.LikedProductList>> response = testRestTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            LikeV1Response.LikedProductList result = response.getBody().data();
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).id()).isEqualTo(testProduct1.getId());

        }

        @DisplayName("[실패] 존재하지 않는 유저가 조회하면 404 Not Found를 반환한다.")
        @Test
        void failure_whenUserDoesNotExist() {
            String url = "/api/v1/likes/" + "non-existent-user" + "/products";
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", "non-existent-user");
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<Void>> response = testRestTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().meta().message()).contains("해당 사용자를 찾을 수 없습니다.");
        }
    }
}
