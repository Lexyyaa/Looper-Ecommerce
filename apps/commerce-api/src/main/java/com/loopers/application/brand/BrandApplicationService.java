package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.controller.brand.BrandV1Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BrandApplicationService implements BrandUsecase {

    private final BrandService brandService;

    @Override
    @Transactional(readOnly = true)
    public BrandV1Response getBrandInfo(Long brandId) {
        Brand brand = brandService.get(brandId);
        return BrandV1Response.from(brand);
    }
}
