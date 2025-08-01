package com.loopers.interfaces.api.controller.brand;

import com.loopers.domain.brand.Brand;

public record BrandResponse(
        Long id,
        String name,
        String description
) {
    public static BrandResponse from(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getDescription()
        );
    }
}
