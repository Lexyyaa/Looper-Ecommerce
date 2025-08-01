package com.loopers.interfaces.api.controller.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductInfo;

import java.util.List;

public class ProductResponse{

    public record Summaries(
            long totalCount,
            List<Summary> summaries
    ) {
        public static Summaries from(List<ProductInfo.Summary> infos) {
            List<Summary> summaries = infos.stream()
                    .map(Summary::from)
                    .toList();
            return new Summaries(infos.size(), summaries);
        }
    }

    public record Summary(
            Long id,
            String name,
            int price,
            long likeCount,
            String status
    ) {
        public static Summary from(ProductInfo.Summary info) {
            return new Summary(
                    info.id(),
                    info.name(),
                    info.price(),
                    info.likeCount(),
                    info.status()
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
        public static Detail from(ProductInfo.Detail info) {
            return new Detail(
                    info.id(),
                    info.name(),
                    info.status(),
                    info.brandId(),
                    info.brandName(),
                    info.likeCount(),
                    info.skus().stream().map(Sku::from).toList()
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
        public static Sku from(ProductInfo.Sku info) {
            return new Sku(
                    info.id(),
                    info.sku(),
                    info.price(),
                    info.stockTotal(),
                    info.stockReserved()
            );
        }
    }


}
