package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Slf4j
@SpringBootTest
@DisplayName("BrandService 통합 테스트")
class BrandServiceIntegrationTest {

    @Autowired
    private BrandService brandService;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown(){
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("[브랜드 단건 조회]")
    class GetBrandInfo {

        @Test
        @DisplayName("[성공] 활성화된 브랜드 조회")
        void success_getBrand_whenActive() {
            Brand brand = brandRepository.save(Brand.create("나이키", "스포츠"));
            Brand result = brandService.get(brand.getId());

            assertEquals("나이키", result.getName());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 브랜드 조회 시 예외 발생")
        void failure_getBrand_whenNotFound() {
            CoreException exception =  assertThrows(CoreException.class,
                    () -> brandService.get(9999L)
            );

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 비활성화 브랜드 조회 시 예외 발생")
        void failure_getBrand_whenInactive() {
            Brand newBrand = Brand.create("비활성", "숨겨진 브랜드");
            newBrand.deactivate();
            Brand brand = brandRepository.save(newBrand);

            CoreException exception =  assertThrows(CoreException.class,
                    () -> brandService.get(brand.getId())
            );

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
