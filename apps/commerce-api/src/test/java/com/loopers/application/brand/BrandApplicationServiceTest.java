package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.controller.brand.BrandV1Response;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("BrandApplicationService 단위 테스트")
class BrandApplicationServiceTest {

    @Mock
    private BrandService brandService;

    @InjectMocks
    private BrandApplicationService brandApplicationService;

    @Nested
    @DisplayName("[브랜드 정보 조회]")
    class GetBrandInfo {

        @Test
        @DisplayName("[성공] 브랜드 정보를 조회하여 응답 객체로 변환한다.")
        void success_getBrandInfo() {
            Brand brand = Brand.create("나이키", "스포츠 브랜드");
            when(brandService.get(1L)).thenReturn(brand);

            BrandV1Response result = brandApplicationService.getBrandInfo(1L);

            assertEquals("나이키", result.name());
            assertEquals("스포츠 브랜드", result.description());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 브랜드일 경우 NOT_FOUND 예외가 발생한다.")
        void failure_getBrandInfo_notFound() {
            when(brandService.get(999L))
                    .thenThrow(new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 브랜드입니다."));

            CoreException ex = assertThrows(CoreException.class, () -> brandApplicationService.getBrandInfo(999L));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 비활성화된 브랜드일 경우 BAD_REQUEST 예외가 발생한다.")
        void failure_getBrandInfo_inactive() {
            when(brandService.get(2L))
                    .thenThrow(new CoreException(ErrorType.BAD_REQUEST, "비활성화된 브랜드입니다."));

            CoreException ex = assertThrows(CoreException.class, () -> brandApplicationService.getBrandInfo(2L));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}

