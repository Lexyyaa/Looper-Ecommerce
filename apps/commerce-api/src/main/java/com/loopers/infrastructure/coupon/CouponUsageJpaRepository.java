package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageJpaRepository extends JpaRepository<CouponUsage, Long> {
}
