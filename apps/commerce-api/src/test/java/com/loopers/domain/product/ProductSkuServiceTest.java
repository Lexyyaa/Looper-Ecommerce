package com.loopers.domain.product;

import com.loopers.infrastructure.product.ProductSkuJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSkuService 단위 테스트 ")
class ProductSkuServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductSkuService productSkuService;

    @Nested
    @DisplayName("[재고 차감]")
    class DecreaseStock {

//        @Test
        @DisplayName("[성공] SKU 재고를 정상적으로 차감한다.")
        void success_decreaseStock() {
            ProductSku sku = ProductSku.builder().id(1L).stockTotal(5).stockReserved(0).build();
            when(productRepository.findBySkuId(1L)).thenReturn(Optional.of(sku));
            when(productRepository.saveProductSku(sku)).thenReturn(sku);

            productSkuService.reserveStock(1L, 3);

            assertThat(sku.availableQuantity()).isEqualTo(2);
            verify(productRepository).saveProductSku(sku);
        }


//        @Test
        @DisplayName("[성공] 재고를 딱 맞게 차감하면 재고가 0이 된다.")
        void success_decreaseStock_exact() {
            ProductSku sku = ProductSku.builder()
                    .id(1L)
                    .stockTotal(5)
                    .stockReserved(0)
                    .build();

            when(productRepository.findByIdWithOptimisticLock(1L)).thenReturn(Optional.of(sku));

            productSkuService.reserveStock(sku.getId(), 5);

            assertEquals(0, sku.availableQuantity());
            verify(productRepository).saveProductSku(sku);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 SKU일 경우 NOT_FOUND 예외가 발생한다.")
        void failure_decreaseStock_skuNotFound() {
            when(productRepository.findBySkuId(1L)).thenReturn(Optional.empty());

            CoreException ex = assertThrows(CoreException.class, () -> productSkuService.getBySkuId(1L));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        // @Test
        @DisplayName("[실패] 재고 부족 시 BAD_REQUEST 예외가 발생한다.")
        void failure_decreaseStock_insufficientStock() {
            ProductSku sku = ProductSku.builder()
                    .id(1L)
                    .stockTotal(10)
                    .stockReserved(10)
                    .build();

            when(productRepository.findByIdWithOptimisticLock(sku.getId()))
                    .thenReturn(Optional.of(sku));

            CoreException exception = assertThrows(CoreException.class, () ->
                    productSkuService.reserveStock(sku.getId(), 1)
            );

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            verify(productRepository, never()).saveProductSku(any(ProductSku.class));
        }
    }
}
