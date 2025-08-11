package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.interfaces.api.controller.brand.BrandV1Response;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("BrandApplicationService 통합 테스트")
class BrandApplicationServiceIntegrationTest {

    @Autowired
    private BrandApplicationService brandApplicationService;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("[브랜드 단건 조회]")
    class GetBrand {

        @Test
        @DisplayName("[성공] 존재하는 브랜드 ID로 조회하면 브랜드 정보를 반환한다.")
        void success_getBrandById() {
            Brand saved = brandRepository.save(Brand.create("나이키", "스포츠 브랜드"));

            BrandV1Response result = brandApplicationService.getBrandInfo(saved.getId());

            assertThat(result.name()).isEqualTo("나이키");
            assertThat(result.description()).isEqualTo("스포츠 브랜드");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 브랜드 ID로 조회하면 예외가 발생한다.")
        void failure_getBrandById_whenNotExist() {
            // act
            CoreException exception =  assertThrows(CoreException.class,
                    () ->  brandApplicationService.getBrandInfo(9999L)
            );

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);

        }
    }
}
