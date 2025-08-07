package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discount_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "coupon")
@Getter
public abstract class Coupon extends BaseEntity {

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

    public enum DiscountType {
        AMOUNT, RATE
    }

    public enum TargetType {
        CART, PRODUCT, BRAND, CATEGORY
    }

    public abstract BigDecimal discount(BigDecimal originalAmount);
}
