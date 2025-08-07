package com.loopers.interfaces.api.controller.brand;

import com.loopers.domain.brand.Brand;

public record BrandV1Response(
        Long id,
        String name,
        String description
) {
    public static BrandV1Response from(Brand brand) {
        return new BrandV1Response(
                brand.getId(),
                brand.getName(),
                brand.getDescription()
        );
    }
}
