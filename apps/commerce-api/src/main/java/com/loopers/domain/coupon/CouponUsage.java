package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.Order;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_usage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class CouponUsage  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id", nullable = false)
    private UserCoupon userCoupon;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private Coupon.TargetType targetType;

    @Column(name = "discounted_amount", nullable = false)
    private BigDecimal discountedAmount;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    public static CouponUsage create(UserCoupon userCoupon, BigDecimal discountedAmount) {
        Coupon coupon = userCoupon.getCoupon();

        return CouponUsage.builder()
                .userCoupon(userCoupon)
                .discountedAmount(discountedAmount)
                .targetType(coupon.getTargetType())
                .usedAt(LocalDateTime.now())
                .build();
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
