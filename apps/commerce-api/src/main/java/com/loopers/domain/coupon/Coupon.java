package com.loopers.domain.coupon;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discount_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "coupon")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_code", unique = true, nullable = false)
    private String couponCode;

    @Column(name = "coupon_name", nullable = false)
    private String couponName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", insertable = false, updatable = false)
    private DiscountType discountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "discount_amount")
    protected BigDecimal discountAmount;

    @Column(name = "discount_rate")
    protected BigDecimal discountRate;

    @Column(name = "max_discount_amount")
    protected BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount")
    protected BigDecimal minOrderAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    public enum DiscountType {
        AMOUNT, RATE
    }

    public enum TargetType {
        CART, PRODUCT, BRAND, CATEGORY
    }

   public abstract BigDecimal discount(BigDecimal originalAmount);

    @PrePersist
    void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (minOrderAmount == null) minOrderAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
