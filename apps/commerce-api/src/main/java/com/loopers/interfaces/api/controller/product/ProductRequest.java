package com.loopers.interfaces.api.controller.product;

import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductSortType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class ProductRequest{
    public record List(
            @PositiveOrZero Integer page,
            @Positive Integer size,
            Long brandId,
            ProductSortType sortType
    ) {
        public ProductCommand.List toCommand() {
            return new ProductCommand.List(
                    page != null ? page : 0,
                    size != null ? size : 5,
                    brandId,
                    sortType != null ? sortType : ProductSortType.RECENT
            );
        }
    }
}
