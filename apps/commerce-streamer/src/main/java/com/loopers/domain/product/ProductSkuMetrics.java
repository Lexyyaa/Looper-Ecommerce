package com.loopers.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "product_sku_metrics",
        indexes = {
                @Index(name = "idx_product_sku_metrics_updated_at", columnList = "updatedAt")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSkuMetrics {

    @Id
    private Long productSkuId;

    private Long productId;

    private long salesCnt;

    private Instant updatedAt;

}

