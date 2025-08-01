package com.loopers.interfaces.api.controller.brand;

import com.loopers.application.brand.BrandUsecase;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.controller.user.UserV1Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandUsecase brandUsecase;

    @GetMapping("/{brandId}")
    public ApiResponse<BrandResponse> getBrandInfo(@PathVariable Long brandId) {
        BrandResponse response = brandUsecase.getBrandInfo(brandId);
        return ApiResponse.success(response);
    }
}
