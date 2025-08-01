package com.loopers.application.brand;

import com.loopers.interfaces.api.controller.brand.BrandResponse;

public interface BrandUsecase {
    BrandResponse getBrandInfo(Long brandId);
}
