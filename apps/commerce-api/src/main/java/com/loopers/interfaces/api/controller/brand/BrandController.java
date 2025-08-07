package com.loopers.interfaces.api.controller.brand;

import com.loopers.application.brand.BrandUsecase;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController implements BrandV1ApiSpec {

    private final BrandUsecase brandUsecase;

    @GetMapping("/{brandId}")
    public ApiResponse<BrandV1Response> getBrandInfo(@PathVariable Long brandId) {
        BrandV1Response response = brandUsecase.getBrandInfo(brandId);
        return ApiResponse.success(response);
    }
}
