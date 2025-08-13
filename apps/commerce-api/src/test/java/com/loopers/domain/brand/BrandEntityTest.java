package com.loopers.domain.brand;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Brand Entity 테스트 ")
class BrandEntityTest {

    @Nested
    @DisplayName("[브랜드 생성]")
    class CreateBrand {

        @Test
        @DisplayName("[성공] 정상 값으로 브랜드를 생성한다.")
        void success_createBrand() {
            Brand brand = Brand.create("나이키", "스포츠 브랜드");

            assertEquals("나이키", brand.getName());
            assertEquals("스포츠 브랜드", brand.getDescription());
            assertEquals(Brand.Status.ACTIVE, brand.getStatus());
        }

        @Test
        @DisplayName("[성공] create() 호출 시 상태는 무조건 ACTIVE로 설정된다.")
        void success_createBrand_forceActive() {
            Brand brand = Brand.create("아디다스", "스포츠 브랜드");
            assertEquals(Brand.Status.ACTIVE, brand.getStatus());
        }
    }

    @Nested
    @DisplayName("[활성 상태 확인]")
    class IsActive {

        @Test
        @DisplayName("[성공] ACTIVE 상태이면 true를 반환한다.")
        void success_isActive_true() {
            Brand brand = Brand.builder()
                    .name("나이키")
                    .status(Brand.Status.ACTIVE)
                    .build();

            assertTrue(brand.isActive());
        }

        @Test
        @DisplayName("[성공] INACTIVE 상태이면 false를 반환한다.")
        void success_isActive_false() {
            Brand brand = Brand.builder()
                    .name("나이키")
                    .status(Brand.Status.INACTIVE)
                    .build();

            assertFalse(brand.isActive());
        }
    }

    @Nested
    @DisplayName("[브랜드 비활성화]")
    class Deactivate {

        @Test
        @DisplayName("[성공] 브랜드 상태를 INACTIVE로 변경한다.")
        void success_deactivate() {
            Brand brand = Brand.create("나이키", "스포츠 브랜드");
            brand.deactivate();

            assertEquals(Brand.Status.INACTIVE, brand.getStatus());
            assertFalse(brand.isActive());
        }
    }
}
