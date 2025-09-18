package com.loopers.domain.product;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name="product_metrics",
        indexes = @Index(name="idx_product_metrics_updated_at", columnList = "updatedAt"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMetrics {

    @EmbeddedId
    private ProductMetricsId id;

    @Column(name = "view_cnt_delta", nullable = false)
    private long viewCntDelta;

    @Column(name = "like_cnt_delta", nullable = false)
    private long likeCntDelta;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public void addViewDelta(long d){
        if (d > 0){
            this.viewCntDelta += d;
        }
    }

    public void addLikeDelta(long d){
        if (d == 0)
            return;
        long next = this.likeCntDelta + d;
        this.likeCntDelta = Math.max(0, next);
    }

    public static ProductMetrics newDailyRow(Long productId, LocalDate date) {
        return ProductMetrics.builder()
                .id(ProductMetricsId.of(productId, date))
                .viewCntDelta(0L)
                .likeCntDelta(0L)
                .updatedAt(Instant.now())
                .build();
    }
}
