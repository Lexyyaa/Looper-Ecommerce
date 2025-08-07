package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_item")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @Column(name = "product_sku_id", nullable = false, updatable = false)
    private Long productSkuId;

    @Column(nullable = false)
    private int quantity;

    public static OrderItem create(Long productSkuId, int quantity) {
        return OrderItem.builder()
                .productSkuId(productSkuId)
                .quantity(quantity)
                .build();
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
