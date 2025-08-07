package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("[상품 목록 조회]")
    class GetProductSummaries {

        @Test
        @DisplayName("[성공] 첫 페이지(page=0, size=10) 최신순 조회")
        void success_firstPage_latestSort() {
            ProductSummaryProjection projection = mock(ProductSummaryProjection.class);
            when(projection.getId()).thenReturn(1L);
            when(projection.getName()).thenReturn("상품1");
            when(projection.getMinPrice()).thenReturn(1000);
            when(projection.getLikeCount()).thenReturn(5L);
            when(projection.getStatus()).thenReturn(Product.Status.ACTIVE);
            when(projection.getCreatedAt()).thenReturn(LocalDateTime.now());

            when(productRepository.findProductSummaries(1L, ProductSortType.RECENT, 1, 10))
                    .thenReturn(List.of(projection));

            var result = productService.getProductSummaries(
                    new ProductCommand.List(1, 10, 1L, ProductSortType.RECENT)
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("상품1");
        }

        @Test
        @DisplayName("[성공] 두 번째 페이지(page=1, size=5) 좋아요순 조회")
        void success_secondPage_likesDesc() {
            ProductSummaryProjection projection = mock(ProductSummaryProjection.class);
            when(projection.getId()).thenReturn(6L);
            when(projection.getName()).thenReturn("상품6");
            when(projection.getMinPrice()).thenReturn(2000);
            when(projection.getLikeCount()).thenReturn(10L);
            when(projection.getStatus()).thenReturn(Product.Status.ACTIVE);
            when(projection.getCreatedAt()).thenReturn(LocalDateTime.now());

            when(productRepository.findProductSummaries(1L, ProductSortType.LIKE, 1, 5))
                    .thenReturn(List.of(projection));

            var result = productService.getProductSummaries(
                    new ProductCommand.List(1, 5, 1L, ProductSortType.LIKE)
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(6L);
        }

        @Test
        @DisplayName("[경계값] size=0이면 빈 목록 반환 (또는 예외 처리)")
        void boundary_zeroSize() {
            when(productRepository.findProductSummaries(any(), any(), anyInt(), eq(0)))
                    .thenReturn(List.of());

            var result = productService.getProductSummaries(
                    new ProductCommand.List(1, 0, 1L, ProductSortType.RECENT)
            );

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("[단일 상품 조회]")
    class GetProduct {

        @Test
        @DisplayName("[성공] 판매중인 상품을 조회한다.")
        void success_getActiveProduct() {
            Product product = Product.builder()
                    .id(1L)
                    .status(Product.Status.ACTIVE)
                    .build();

            when(productRepository.findBy(1L)).thenReturn(Optional.of(product));

            Product result = productService.getProduct(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 상품일 경우 NOT_FOUND 예외 발생")
        void failure_notFound() {
            when(productRepository.findBy(999L)).thenReturn(Optional.empty());

            CoreException ex = assertThrows(CoreException.class, () -> productService.getProduct(999L));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 판매중이 아닌 상품일 경우 BAD_REQUEST 예외 발생")
        void failure_inactiveProduct() {
            Product product = Product.builder()
                    .id(2L)
                    .status(Product.Status.SOLD_OUT)
                    .build();

            when(productRepository.findBy(2L)).thenReturn(Optional.of(product));

            CoreException ex = assertThrows(CoreException.class, () -> productService.getProduct(2L));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("[상품 상태 업데이트]")
    class UpdateStatus {

        @Test
        @DisplayName("[성공] 모든 재고가 소진된 경우 상품 상태를 SOLD_OUT으로 변경한다.")
        void success_updateStatus_soldOut() {
            Product product = Product.builder()
                    .id(1L)
                    .status(Product.Status.ACTIVE)
                    .build();

            when(productRepository.findBy(1L)).thenReturn(Optional.of(product));

            productService.updateStatus(true, 1L);

            assertThat(product.getStatus()).isEqualTo(Product.Status.SOLD_OUT);
            verify(productRepository).saveProduct(product);
        }

        @Test
        @DisplayName("[성공] 재고가 남아 있는 경우 상태 변경하지 않는다.")
        void success_updateStatus_stockAvailable() {
            productService.updateStatus(false, 1L);
            verify(productRepository, never()).saveProduct(any());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 상품 ID일 경우 NOT_FOUND 예외 발생")
        void failure_updateStatus_notFound() {
            when(productRepository.findBy(999L)).thenReturn(Optional.empty());

            CoreException ex = assertThrows(CoreException.class, () -> productService.updateStatus(true, 999L));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
