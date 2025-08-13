package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.product.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductApplicationService")
class ProductApplicationServiceTest {

    @Mock
    private ProductService productService;
    @Mock
    private ProductSkuService productSkuService;
    @Mock
    private ProductSummaryService productSummaryService;
    @Mock
    private LikeService likeService;
    @Mock
    private BrandService brandService;

    @InjectMocks
    private ProductApplicationService productApplicationService;

    @Nested
    @DisplayName("[상품 목록 조회]")
    class GetProductList {


        static Stream<Arguments> providePagingAndSortOptions() {
            return Stream.of(
                    Arguments.of(0, 10, ProductSortType.RECENT),
                    Arguments.of(1, 5, ProductSortType.RECENT),
                    Arguments.of(0, 10, ProductSortType.LIKE),
                    Arguments.of(1, 5, ProductSortType.LIKE),
                    Arguments.of(0, 20, ProductSortType.LOW_PRICE)
            );
        }

        @ParameterizedTest(name = "[성공] page={0}, size={1}, sortType={2} 조합으로 상품목록 조회")
        @MethodSource("providePagingAndSortOptions")
        void success_getProductSummaries_variousCases(int page, int size, ProductSortType sortType) {
            ProductSummary summary = new ProductSummary(
                    1L,
                    "상품1",
                    1000,
                    3L,
                    Product.Status.ACTIVE,
                    LocalDateTime.now()
            );

            when(productSummaryService.getProductSummaries(any()))
                    .thenReturn(List.of(summary));

            var result = productApplicationService.getProductSummaries(
                    new ProductCommand.List(page, size, 1L, sortType)
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("상품1");

            verify(productSummaryService).getProductSummaries(any());
        }

        static Stream<Arguments> provideInvalidPaging() {
            return Stream.of(
                    Arguments.of(-1, 10),
                    Arguments.of(0, 0),
                    Arguments.of(0, -5)
            );
        }

        @ParameterizedTest(name = "[실패] page={0}, size={1} 인 경우 BAD_REQUEST 발생")
        @MethodSource("provideInvalidPaging")
        void failure_invalidPaging(int page, int size) {
            when(productSummaryService.getProductSummaries(any()))
                    .thenThrow(new CoreException(ErrorType.BAD_REQUEST, "잘못된 페이징 요청"));

            CoreException ex = assertThrows(CoreException.class, () ->
                    productApplicationService.getProductSummaries(
                            new ProductCommand.List(page, size, 1L, ProductSortType.RECENT)
                    )
            );
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("[상품 상세 조회]")
    class GetProductDetail {

        @Test
        @DisplayName("[성공] 상품 상세 정보를 브랜드, 좋아요 수와 함께 조회한다.")
        void success_getProductDetail() {
            Product product = Product.builder()
                    .id(1L)
                    .brandId(2L)
                    .name("상품1")
                    .status(Product.Status.ACTIVE)
                    .build();
            ProductSku sku = ProductSku.builder()
                    .id(10L)
                    .product(product)
                    .stockTotal(100)
                    .stockReserved(0)
                    .build();
            Brand brand = Brand.builder()
                    .id(2L)
                    .name("브랜드A")
                    .status(Brand.Status.ACTIVE)
                    .build();

            when(productService.getProduct(1L)).thenReturn(product);
            when(productSkuService.getByProductId(1L)).thenReturn(List.of(sku));
            when(likeService.getLikeCount(1L, LikeTargetType.PRODUCT)).thenReturn(5L);
            when(brandService.get(2L)).thenReturn(brand);

            var result = productApplicationService.getProductDetail(1L);

            assertThat(result.name()).isEqualTo("상품1");
            assertThat(result.brandName()).isEqualTo("브랜드A");
            assertThat(result.likeCount()).isEqualTo(5L);

            verify(productService).getProduct(1L);
            verify(productSkuService).getByProductId(1L);
            verify(likeService).getLikeCount(1L, LikeTargetType.PRODUCT);
            verify(brandService).get(2L);
        }

        @Test
        @DisplayName("[실패] 상품이 존재하지 않으면 NOT_FOUND 예외 발생")
        void failure_getProductDetail_productNotFound() {
            when(productService.getProduct(1L))
                    .thenThrow(new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품"));

            CoreException ex = assertThrows(CoreException.class,
                    () -> productApplicationService.getProductDetail(1L));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 브랜드가 존재하지 않으면 NOT_FOUND 예외 발생")
        void failure_getProductDetail_brandNotFound() {
            Product product = Product.builder()
                    .id(1L)
                    .brandId(2L)
                    .name("상품1")
                    .status(Product.Status.ACTIVE)
                    .build();

            when(productService.getProduct(1L)).thenReturn(product);
            when(brandService.get(2L))
                    .thenThrow(new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 브랜드"));

            CoreException ex = assertThrows(CoreException.class,
                    () -> productApplicationService.getProductDetail(1L));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}

