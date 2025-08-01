package com.loopers.domain.product;

import java.time.LocalDateTime;
import java.util.List;

public class ProductInfo {

    public record Summary(
            Long id,
            String name,
            int price,
            long likeCount,
            String status,
            LocalDateTime createdAt
    ) {
        public static Summary from(ProductSummary summary) {
            return new Summary(
                    summary.id(),
                    summary.name(),
                    summary.price(),
                    summary.likeCount(),
                    summary.status().name(),
                    summary.createdAt()
            );
        }
    }

    public record Detail(
            Long id,
            String name,
            Product.Status status,
            Long brandId,
            String brandName,
            long likeCount,
            List<Sku> skus
    ) {
        public static Detail from(Product product, String brandName, List<ProductSku> skus, long likeCount) {
            return new Detail(
                    product.getId(),
                    product.getName(),
                    product.getStatus(),
                    product.getBrandId(),
                    brandName,
                    likeCount,
                    skus.stream().map(Sku::from).toList()
            );
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
