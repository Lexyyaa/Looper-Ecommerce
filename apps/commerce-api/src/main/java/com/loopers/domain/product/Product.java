package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Product.Status status;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    public enum Status {
        ACTIVE,
        INACTIVE,
        SOLD_OUT
    }

    public static Product create(String name, Product.Status status, Long brandId) {
        return Product.builder()
                .name(name)
                .status(Product.Status.ACTIVE)
                .brandId(brandId)
                .build();
    }

    public boolean isAvailable() {
        return this.status == Product.Status.ACTIVE;
    }

    public void changeStatus(Product.Status status) {
        this.status = status;
    }
}
