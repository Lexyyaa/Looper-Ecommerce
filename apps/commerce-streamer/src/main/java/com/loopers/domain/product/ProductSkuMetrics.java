package com.loopers.domain.product;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "product_sku_metrics",
        indexes = {
                @Index(name = "idx_product_sku_metrics_updated_at", columnList = "updated_at"),
                @Index(name = "idx_product_sku_metrics_product_id_date", columnList = "product_id,date")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSkuMetrics {

    @EmbeddedId
    private ProductSkuMetricsId id;  // (skuId, date)

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sales_cnt_delta", nullable = false)
    private long salesCntDelta;

    @Column(name = "updated_at", nullable=false)
    private Instant updatedAt;

    public void addSalesDelta(long delta){
        if (delta > 0) {
            this.salesCntDelta += delta;
            this.updatedAt = Instant.now();
        }
    }

    public static ProductSkuMetrics newDailyRow(Long productId, Long productSkuId, LocalDate date) {
        return ProductSkuMetrics.builder()
                .id(ProductSkuMetricsId.of(productSkuId, date))
                .productId(productId)
                .salesCntDelta(0L)
                .updatedAt(Instant.now())
                .build();
    }
}
