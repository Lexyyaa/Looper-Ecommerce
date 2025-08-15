package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

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

    @Column(name = "like_cnt", nullable = false)
    private Long likeCnt;

    @Column(name = "min_price", nullable = false)
    private Long minPrice;

    public enum Status {
        ACTIVE,
        INACTIVE,
        SOLD_OUT,
        TEMPORARILY_UNAVAILABLE,
    }

    public static Product create(String name, Product.Status status, Long brandId) {

        return Product.builder()
                .name(name)
                .status(status)
                .brandId(brandId)
                .likeCnt(0L)
                .minPrice(0L)
                .build();
    }

    public void updateLikeCnt(Long likeCnt){
        this.likeCnt = likeCnt;
    }

    public void updateMinPrice(Long minPrice){
        this.minPrice = minPrice;
    }

    public boolean isAvailable() {
        return this.status == Product.Status.ACTIVE;
    }

    public void changeStatus(Product.Status status) {
        this.status = status;
    }
}
