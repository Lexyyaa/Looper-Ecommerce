package com.loopers.domain.coupon;

import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Coupon")
public class CouponEntityTest {

    @Nested
    @DisplayName("[정액 할인 - 장바구니 쿠폰]")
    class AmountCartCouponTest{

        @Test
        @DisplayName("[성공] 원금액에서 정액을 차감한다")
        void success_discount() {
            var coupon = FixedAmountCoupon.builder()
                    .couponCode("CART-AMOUNT-1000")
                    .couponName("장바구니 천원 할인 쿠폰")
                    .targetType(Coupon.TargetType.CART)
                    .minOrderAmount(BigDecimal.valueOf(5000))
                    .discountAmount(BigDecimal.valueOf(1000))
                    .build();

            BigDecimal result = coupon.discount(BigDecimal.valueOf(5000));

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(4000));
        }

        @Test
        @DisplayName("[성공] 차감 후 0원 미만이면 0원처리한다")
        void success_discount_clamped_to_zero() {
            var coupon = FixedAmountCoupon.builder()
                    .couponCode("CART-AMOUNT-10000")
                    .couponName("장바구니 만원 할인 쿠폰")
                    .targetType(Coupon.TargetType.CART)
                    .minOrderAmount(BigDecimal.valueOf(1000))
                    .discountAmount(BigDecimal.valueOf(10000))
                    .build();

            BigDecimal result = coupon.discount(BigDecimal.valueOf(5000));

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(0));
        }
    }

    @Nested
    @DisplayName("[정률 할인 쿠폰]")
    class RateCouponTest{
        @Test
        @DisplayName("[성공] 정률(10%)로 가격을 계산한다")
        void success_discount_10percent() {
            var coupon = FixedRateCoupon.builder()
                    .couponCode("CART-RATE-10")
                    .couponName("장바구니 10% 할인 쿠폰")
                    .targetType(Coupon.TargetType.CART)
                    .minOrderAmount(BigDecimal.valueOf(0))
                    .discountRate(BigDecimal.valueOf(10))
                    .maxDiscountAmount(BigDecimal.valueOf(10000))
                    .build();

            BigDecimal result = coupon.discount(BigDecimal.valueOf(12345));
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(11110));
        }

        @Test
        @DisplayName("[성공] 결과 금액은 0원 미만으로 내려가지 않는다")
        void success_discount_not_negative() {
            var coupon = FixedRateCoupon.builder()
                    .couponCode("CART-RATE-10")
                    .couponName("장바구니 90% 할인 쿠폰")
                    .targetType(Coupon.TargetType.CART)
                    .minOrderAmount(BigDecimal.valueOf(0))
                    .discountRate(BigDecimal.valueOf(100))
                    .build();

            BigDecimal result = coupon.discount(BigDecimal.valueOf(999));
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(0));
        }
    }
}
