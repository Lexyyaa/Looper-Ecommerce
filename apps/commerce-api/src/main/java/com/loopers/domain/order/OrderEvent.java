package com.loopers.domain.order;

import com.loopers.domain.coupon.UserCoupon;

public class OrderEvent {
    public record CouponUsed(
           Order order,
           UserCoupon userCoupon
    ) {}

    public record ReCalStock(
            Order order
    ) {}
}



