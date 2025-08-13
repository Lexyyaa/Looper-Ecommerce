package com.loopers.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product Entity")
class ProductEntityTest {

    @Nested
    @DisplayName("[상품 생성]")
    class CreateProduct {

        @Test
        @DisplayName("[성공] 정상 값으로 상품을 생성한다.")
        void success_createProduct() {
            Product product = Product.create("테스트 상품", Product.Status.ACTIVE, 10L);

            assertEquals("테스트 상품", product.getName());
            assertEquals(Product.Status.ACTIVE, product.getStatus());
            assertEquals(10L, product.getBrandId());
        }

        @Test
        @DisplayName("[성공] create() 팩토리 메서드는 상태를 무조건 ACTIVE로 설정한다.")
        void success_createProduct_forceActive() {
            Product product = Product.create("테스트 상품", Product.Status.ACTIVE, 10L);

            assertEquals(Product.Status.ACTIVE, product.getStatus());
        }
    }

    @Nested
    @DisplayName("[판매 가능 여부 확인]")
    class IsAvailable {

        @Test
        @DisplayName("[성공] ACTIVE 상태일 경우 판매 가능하다.")
        void success_isAvailable_active() {
            Product product = Product.builder()
                    .name("상품")
                    .status(Product.Status.ACTIVE)
                    .brandId(10L)
                    .build();

            assertTrue(product.isAvailable());
        }

        @ParameterizedTest
        @EnumSource(value = Product.Status.class, names = {"INACTIVE", "SOLD_OUT"})
        @DisplayName("[성공] INACTIVE 또는 SOLD_OUT 상태일 경우 판매 불가하다.")
        void success_isAvailable_false(Product.Status status) {
            Product product = Product.builder()
                    .name("상품")
                    .status(status)
                    .brandId(10L)
                    .build();

            assertFalse(product.isAvailable());
        }
    }

    @Nested
    @DisplayName("[상태 변경]")
    class ChangeStatus {

        @Test
        @DisplayName("[성공] 상품 상태를 변경한다.")
        void success_changeStatus() {
            Product product = Product.create("상품", Product.Status.ACTIVE, 10L);

            product.changeStatus(Product.Status.SOLD_OUT);

            assertEquals(Product.Status.SOLD_OUT, product.getStatus());
        }
    }
}

