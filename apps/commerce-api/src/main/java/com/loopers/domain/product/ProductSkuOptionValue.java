package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_sku_option_value", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sku_id", "option_value_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductSkuOptionValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id")
    private ProductSku sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_value_id", nullable = false, updatable = false)
    private ProductOptionValue optionValue;

    public static ProductSkuOptionValue create(ProductSku sku, ProductOptionValue optionValue) {
        return ProductSkuOptionValue.builder()
                .sku(sku)
                .optionValue(optionValue)
                .build();
    }
}
