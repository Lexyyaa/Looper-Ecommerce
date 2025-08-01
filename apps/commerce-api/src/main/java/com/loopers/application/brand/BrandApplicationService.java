package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.controller.brand.BrandResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandApplicationService implements BrandUsecase {

    private final BrandService brandService;

    @Override
    public BrandResponse getBrandInfo(Long brandId) {
        Brand brand = brandService.get(brandId);
        return BrandResponse.from(brand);
    }
}
