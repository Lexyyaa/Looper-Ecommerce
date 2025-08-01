package com.loopers.domain.product;

public class ProductCommand {

    public record List(
            int page,
            int size,
            Long brandId,
            ProductSortType sortType
    ) {
    }
}
