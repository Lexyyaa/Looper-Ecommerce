package com.loopers.domain.brand;

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
@DisplayName("BrandService")
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    @Nested
    @DisplayName("[브랜드 조회]")
    class GetBrand {

        @Test
        @DisplayName("[성공] 존재하는 활성화된 브랜드를 조회한다.")
        void success_getBrand() {
            Brand brand = Brand.create("나이키", "스포츠 브랜드");
            when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

            Brand result = brandService.get(1L);

            assertEquals("나이키", result.getName());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 브랜드 ID일 경우 NOT_FOUND 예외가 발생한다.")
        void failure_getBrand_notFound() {
            when(brandRepository.findById(999L)).thenReturn(Optional.empty());

            CoreException ex = assertThrows(CoreException.class, () -> brandService.get(999L));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 비활성화된 브랜드일 경우 BAD_REQUEST 예외가 발생한다.")
        void failure_getBrand_inactive() {
            Brand brand = Brand.builder()
                    .name("아디다스")
                    .status(Brand.Status.INACTIVE)
                    .build();

            when(brandRepository.findById(2L)).thenReturn(Optional.of(brand));

            CoreException ex = assertThrows(CoreException.class, () -> brandService.get(2L));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}

