package com.loopers.domain.coupon;

import java.util.Optional;

public interface CouponRepository {
    Optional<UserCoupon> findByUserCouponId(Long userCouponId);
    Coupon save(Coupon coupon);
    CouponUsage save(CouponUsage couponUsage);
    UserCoupon save(UserCoupon userCoupon);
    Optional<UserCoupon> findByIdWithPessimisticLock(Long userCouponId);
}
