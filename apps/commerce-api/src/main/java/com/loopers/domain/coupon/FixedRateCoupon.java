package com.loopers.domain.coupon;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

@Slf4j
@Entity
@DiscriminatorValue("RATE")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedRateCoupon extends Coupon {

    public static FixedRateCoupon create(
            String couponCode,
            String couponName,
            int quantity,
            TargetType targetType,
            BigDecimal minOrderAmount,
            BigDecimal discountRate,
            BigDecimal maxDiscountAmount) {

        return FixedRateCoupon.builder()
                .couponCode(couponCode)
                .couponName(couponName)
                .quantity(quantity)
                .targetType(targetType)
                .minOrderAmount(minOrderAmount)
                .discountRate(discountRate)
                .maxDiscountAmount(maxDiscountAmount)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Override
    public BigDecimal discount(BigDecimal originalAmount) {

        BigDecimal discountAmount = originalAmount.multiply(this.getDiscountRate().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        if (this.getMaxDiscountAmount() != null && discountAmount.compareTo(this.getMaxDiscountAmount()) > 0) {
            discountAmount = this.getMaxDiscountAmount();
        }

        BigDecimal finalPrice = originalAmount.subtract(discountAmount);
        return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice.setScale(0, RoundingMode.DOWN);
    }
}
