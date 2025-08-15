package com.loopers.domain.product;

import java.util.stream.Collectors;

public class ProductCommand {

    public record List(
            int page,
            int size,
            Long brandId,
            ProductSortType sortType
    ) {
    }

    public record Update(
            Long id,
            String name,
            Product.Status status,
            Long brandId,
            String brandName,
            java.util.List<Sku> skus
    ) {
        public java.util.List<ProductSku> to(Product product) {
            return this.skus.stream()
                    .map(skuDto -> ProductSku.create(product, skuDto.sku(), skuDto.price(), skuDto.stockTotal(), skuDto.stockReserved()))
                    .collect(Collectors.toList());
        }
    }

    public record Sku(
            Long id,
            String sku,
            int price,
            int stockTotal,
            int stockReserved
    ) {
        public static Sku from(ProductSku sku) {
            return new Sku(
                    sku.getId(),
                    sku.getSku(),
                    sku.getPrice(),
                    sku.getStockTotal(),
                    sku.getStockReserved()
            );
        }
    }
}
