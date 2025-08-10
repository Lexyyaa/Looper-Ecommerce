package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponUsage;
import com.loopers.domain.coupon.UserCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;
    private final UserCouponJpaRepository userCouponJpaRepository;
    private final CouponUsageJpaRepository couponUsageJpaRepository;

    @Override
    public Optional<UserCoupon> findByUserCouponId(Long userCouponId) {
        return userCouponJpaRepository.findById(userCouponId);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return couponJpaRepository.save(coupon);
    }

    @Override
    public CouponUsage save(CouponUsage couponUsage) {
        return couponUsageJpaRepository.save(couponUsage);
    }

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        return userCouponJpaRepository.save(userCoupon);
    }

    @Override
    public Optional<UserCoupon> findByIdWithPessimisticLock(Long userCouponId) {
        return userCouponJpaRepository.findByIdWithPessimisticLock(userCouponId);
    }
}
