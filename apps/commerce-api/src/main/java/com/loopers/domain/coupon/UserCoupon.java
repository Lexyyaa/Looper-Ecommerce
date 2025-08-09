package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class UserCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CouponStatus status;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    public enum CouponStatus {
        ISSUED, USED, CANCELED
    }

    public static UserCoupon create(Long userId,
                                    Coupon coupon,
                                    CouponStatus status,
                                    LocalDateTime issuedAt,
                                    LocalDateTime expiredAt) {
        return UserCoupon.builder()
                .userId(userId)
                .coupon(coupon)
                .status(status)
                .issuedAt(issuedAt)
                .expiredAt(expiredAt)
                .build();
    }

    public void checkAvailability(Long currentUserId) {
        if (!this.userId.equals(currentUserId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "본인의 쿠폰만 사용할 수 있습니다.");
        }

        if (this.status != CouponStatus.ISSUED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용할 수 없는 상태의 쿠폰입니다.");
        }

        if (LocalDateTime.now().isAfter(this.expiredAt)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "만료된 쿠폰입니다.");
        }
    }
    public void use() {
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
}
