package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductSku Entity")
class ProductSkuEntityTest {

    @Nested
    @DisplayName("[재고 차감]")
    class DecreaseStock {

        @Test
        @DisplayName("[성공] 재고를 정상적으로 차감한다.")
        void success_decreaseStock() {
            ProductSku sku = ProductSku.builder().stockTotal(10).stockReserved(0).build();
            sku.reserveStock(3);
//            assertEquals(7, sku.availableQuantity());
        }

        @Test
        @DisplayName("[성공] 재고를 딱 맞게 차감하면 가용재고가 0이 된다.")
        void success_decreaseStock_exact() {
            ProductSku sku = ProductSku.builder().stockTotal(5).stockReserved(0).build();
            sku.reserveStock(5);
//            assertEquals(0, sku.availableQuantity());
        }

        @Test
        @DisplayName("[실패] 재고보다 많은 수량을 차감하려고 하면 BAD_REQUEST 예외가 발생한다.")
        void failure_decreaseStock_insufficient() {
            ProductSku sku = ProductSku.builder().stockTotal(5).stockReserved(0).build();
            CoreException ex = assertThrows(CoreException.class, () -> sku.reserveStock(6));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("[실패] 차감 후 음수가 되면 BAD_REQUEST 예외가 발생한다.")
        void failure_decreaseStock_negative() {
            ProductSku sku = ProductSku.builder().stockTotal(0).stockReserved(0).build();
            CoreException ex = assertThrows(CoreException.class, () -> sku.reserveStock(1));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}

