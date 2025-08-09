package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserCoupon 테스트")
class UserCouponEntityTest {

    @Test
    @DisplayName("[성공] 정상적인 쿠폰일 경우 예외 없이 통과된다")
    void success_checkAvailability_whenValidCoupon() {
        var coupon = FixedAmountCoupon.builder()
                .couponCode("CART-AMOUNT-1000")
                .couponName("장바구니 천원 할인 쿠폰")
                .targetType(Coupon.TargetType.CART)
                .minOrderAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.valueOf(1000))
                .build();

        var now = LocalDateTime.now();
        var userCoupon = UserCoupon.create(
                1L,
                coupon,
                UserCoupon.CouponStatus.ISSUED,
                now,
                now.plusDays(1)
        );

        assertDoesNotThrow(() -> userCoupon.checkAvailability(1L));
    }

    @Test
    @DisplayName("[실패] 본인의 쿠폰이 아닌 경우 예외가 발생한다")
    void failure_checkAvailability_whenUserIdDoesNotMatch() {
        var coupon = FixedAmountCoupon.builder()
                .couponCode("CART-AMOUNT-1000")
                .couponName("장바구니 천원 할인 쿠폰")
                .targetType(Coupon.TargetType.CART)
                .minOrderAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.valueOf(1000))
                .build();

        var now = LocalDateTime.now();
        var userCoupon = UserCoupon.create(
                1L,
                coupon,
                UserCoupon.CouponStatus.ISSUED,
                now,
                now.plusDays(1)
        );

        assertThatThrownBy(() -> userCoupon.checkAvailability(2L))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("본인의 쿠폰만 사용할 수 있습니다.");
    }

    @Test
    @DisplayName("[실패] 이미 사용된 쿠폰일 경우 예외가 발생한다")
    void failure_checkAvailability_whenCouponAlreadyUsed() {
        var coupon = FixedAmountCoupon.builder()
                .couponCode("CART-AMOUNT-1000")
                .couponName("장바구니 천원 할인 쿠폰")
                .targetType(Coupon.TargetType.CART)
                .minOrderAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.valueOf(1000))
                .build();

        var now = LocalDateTime.now();
        var userCoupon = UserCoupon.create(
                1L,
                coupon,
                UserCoupon.CouponStatus.USED,
                now,
                now.plusDays(1)
        );

        assertThatThrownBy(() -> userCoupon.checkAvailability(1L))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("사용할 수 없는 상태의 쿠폰입니다.");
    }

    @Test
    @DisplayName("[실패] 만료된 쿠폰일 경우 예외가 발생한다")
    void failure_checkAvailability_whenCouponExpired() {
        var coupon = FixedAmountCoupon.builder()
                .couponCode("CART-AMOUNT-1000")
                .couponName("장바구니 천원 할인 쿠폰")
                .targetType(Coupon.TargetType.CART)
                .minOrderAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.valueOf(1000))
                .build();

        var now = LocalDateTime.now();
        var userCoupon = UserCoupon.create(
                1L,
                coupon,
                UserCoupon.CouponStatus.ISSUED,
                now,
                now.minusDays(1)
        );

        assertThatThrownBy(() -> userCoupon.checkAvailability(1L))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("만료된 쿠폰입니다.");
    }

    @Test
    @DisplayName("[성공] 쿠폰 사용 시 상태가 USED로 변경되고 usedAt이 설정된다")
    void success_useCoupon_whenIssued() {
        var coupon = FixedAmountCoupon.builder()
                .couponCode("CART-AMOUNT-1000")
                .couponName("장바구니 천원 할인 쿠폰")
                .targetType(Coupon.TargetType.CART)
                .minOrderAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.valueOf(1000))
                .build();

        var now = LocalDateTime.now();
        var userCoupon = UserCoupon.create(
                1L,
                coupon,
                UserCoupon.CouponStatus.ISSUED,
                now,
                now.plusDays(1)
        );

        userCoupon.use();

        assertEquals(UserCoupon.CouponStatus.USED, userCoupon.getStatus());
        assertNotNull(userCoupon.getUsedAt(), "사용일시가 설정되어야 합니다.");
    }

    @Test
    @DisplayName("[실패] 취소(CANCELED) 상태의 쿠폰은 사용할 수 없다")
    void failure_checkAvailability_whenCouponCanceled() {
        var coupon = FixedAmountCoupon.builder()
                .couponCode("CART-AMOUNT-1000")
                .couponName("장바구니 천원 할인 쿠폰")
                .targetType(Coupon.TargetType.CART)
                .minOrderAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.valueOf(1000))
                .build();

        var now = LocalDateTime.now();
        var userCoupon = UserCoupon.create(
                1L,
                coupon,
                UserCoupon.CouponStatus.CANCELED,
                now,
                now.plusDays(1)
        );

        assertThatThrownBy(() -> userCoupon.checkAvailability(1L))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("사용할 수 없는 상태의 쿠폰입니다.");
    }
}
