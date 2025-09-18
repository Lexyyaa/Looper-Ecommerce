package com.loopers.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class ProductSkuMetricsId {
    @Column(name = "product_sku_id", nullable = false)
    private Long productSkuId;

    @Column(name = "date", nullable = false)
    private LocalDate date;
}

