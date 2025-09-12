package com.loopers.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;


@Entity
@Table(name="product_metrics",
        indexes = @Index(name="idx_product_metrics_updated_at", columnList = "updatedAt"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductMetrics {

    @Id
    private Long productId;

    private long likeCnt;

    private long viewCnt;

    private Instant updatedAt;

    public void increaseViewCnt(long amount){
        if (amount > 0) {
            this.viewCnt += amount; this.updatedAt = Instant.now();
        }
    }

    public void setLikeCntLatest(long latest){
        this.likeCnt = Math.max(0, latest);
        this.updatedAt = Instant.now();
    }
}
