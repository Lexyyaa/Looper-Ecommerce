package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_option_value")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductOptionValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private ProductOption option;

    @Column(name = "value", nullable = false)
    private String value;

    public static ProductOptionValue create(ProductOption option, String value) {
        return ProductOptionValue.builder()
                .option(option)
                .value(value)
                .build();
    }
}
