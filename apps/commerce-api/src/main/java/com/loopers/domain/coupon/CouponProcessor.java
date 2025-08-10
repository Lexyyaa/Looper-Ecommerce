package com.loopers.domain.coupon;

import java.math.BigDecimal;

public interface CouponProcessor {

    Coupon.TargetType getTargetType();

    void validate(UserCoupon userCoupon, BigDecimal originalPrice);

    BigDecimal apply(BigDecimal originalPrice, Coupon coupon);
}
