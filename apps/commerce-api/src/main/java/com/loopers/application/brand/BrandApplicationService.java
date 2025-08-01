package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.controller.brand.BrandResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BrandApplicationService implements BrandUsecase {

    private final BrandService brandService;

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandInfo(Long brandId) {
        Brand brand = brandService.get(brandId);
        return BrandResponse.from(brand);
    }
}
