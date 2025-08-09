package com.loopers.domain.brand;

import java.util.Optional;

public interface BrandRepository {
    Optional<Brand> findById(long id);
    Brand save(Brand brand);
}
