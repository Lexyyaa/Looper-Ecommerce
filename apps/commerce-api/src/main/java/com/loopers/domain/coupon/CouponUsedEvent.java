package com.loopers.domain.coupon;

import com.loopers.domain.order.Order;

public record CouponUsedEvent(
        Order order,
        UserCoupon userCoupon
) {}
