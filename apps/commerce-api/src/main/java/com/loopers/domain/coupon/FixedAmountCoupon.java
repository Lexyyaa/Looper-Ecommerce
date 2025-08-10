package com.loopers.domain.coupon;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@DiscriminatorValue("AMOUNT")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedAmountCoupon extends Coupon {

    public static FixedAmountCoupon create(
            String couponCode,
            String couponName,
            int quantity,
            TargetType targetType,
            BigDecimal minOrderAmount,
            BigDecimal discountAmount) {

        return FixedAmountCoupon.builder()
                .couponCode(couponCode)
                .couponName(couponName)
                .quantity(quantity)
                .targetType(targetType)
                .minOrderAmount(minOrderAmount)
                .discountAmount(discountAmount)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Override
    public BigDecimal discount(BigDecimal originalAmount) {
        BigDecimal discountedPrice = originalAmount.subtract(this.getDiscountAmount());
        return discountedPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discountedPrice.setScale(0, RoundingMode.DOWN);
    }
}


