package com.loopers.application.brand;

import com.loopers.interfaces.api.controller.brand.BrandV1Response;

public interface BrandUsecase {
    BrandV1Response getBrandInfo(Long brandId);
}
