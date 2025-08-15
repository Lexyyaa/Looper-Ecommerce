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
            Long minPrice,
            Long likeCount,
            List<Sku> skus
    ) {
        public static Detail from(Product product, String brandName, List<ProductSku> skus, Long minPrice, Long likeCount) {
            return new Detail(
                    product.getId(),
                    product.getName(),
                    product.getStatus(),
                    product.getBrandId(),
                    brandName,
                    minPrice,
                    likeCount,
                    skus.stream().map(Sku::from).toList()
            );
        }

        public static Detail fromProduct(Product product,String brandName, List<ProductSku> skus) {
            return new Detail(
                    product.getId(),
                    product.getName(),
                    product.getStatus(),
                    product.getBrandId(),
                    brandName,
                    product.getMinPrice(),
                    product.getLikeCnt(),
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

    public record Item(
            Long id,
            String name,
            Product.Status status,
            Long minPrice,
            Long likeCount,
            List<Sku> skus
    ) {
        public static Item from(Product product, List<ProductSku> skus) {
            return new Item(
                    product.getId(),
                    product.getName(),
                    product.getStatus(),
                    product.getMinPrice(),
                    product.getLikeCnt(),
                    skus.stream().map(Sku::from).toList()
            );
        }
    }
}
