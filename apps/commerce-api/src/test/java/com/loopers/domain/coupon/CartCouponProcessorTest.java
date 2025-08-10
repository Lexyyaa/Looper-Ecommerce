package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CartCouponProcessor")
class CartCouponProcessorTest {

    @Nested
    @DisplayName("[정액 할인 - 장바구니 쿠폰]")
    class AmountCouponTest{

        @Test
        @DisplayName("[성공] 최소주문금액 이상이면 검증 통과한다")
        void success_validate_whenMeetsMinOrder() {
            var processor = new CartCouponProcessor();

            var coupon = FixedAmountCoupon.builder()
                    .couponCode("CART-AMOUNT-1000")
                    .couponName("장바구니 천원 할인 쿠폰")
                    .targetType(Coupon.TargetType.CART)
                    .minOrderAmount(BigDecimal.valueOf(10000))
                    .discountAmount(BigDecimal.valueOf(1000))
                    .build();

            var userCoupon = UserCoupon.create(
                    1L,
                    coupon,
                    UserCoupon.CouponStatus.ISSUED,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1)
            );

            assertDoesNotThrow(() -> processor.validate(userCoupon, BigDecimal.valueOf(10000)));
        }

        @Test
        @DisplayName("[실패] 최소주문금액 미달이면 CoreException 이 발생한다")
        void failure_validate_whenBelowMinOrder() {
            var processor = new CartCouponProcessor();

            var coupon = FixedAmountCoupon.builder()
                    .couponCode("CART-AMOUNT-1000")
                    .couponName("장바구니 천원 할인 쿠폰")
                    .targetType(Coupon.TargetType.CART)
                    .minOrderAmount(BigDecimal.valueOf(10000))
                    .discountAmount(BigDecimal.valueOf(1000))
                    .build();

            var userCoupon = UserCoupon.create(
                    1L,
                    coupon,
                    UserCoupon.CouponStatus.ISSUED,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1)
            );

            assertThatThrownBy(() -> processor.validate(userCoupon, BigDecimal.valueOf(9999)))
                    .isInstanceOf(CoreException.class);
        }
    }

    @Nested
    @DisplayName("[정률 할인 - 장바구니 쿠폰]")
    class RateCouponTest{
        @Test
        @DisplayName("[성공] 최소주문금액 이상이면 검증 통과한다")
        void success_validate_whenMeetsMinOrder_withRateCoupon() {
            var processor = new CartCouponProcessor();

            var rateCoupon = FixedRateCoupon.builder()
                    .couponCode("CART-RATE-10")
                    .couponName("장바구니 10% 할인 쿠폰")
                    .targetType(Coupon.TargetType.CART)
                    .minOrderAmount(BigDecimal.valueOf(10000))
                    .discountRate(BigDecimal.valueOf(10))
                    .build();

            var userCoupon = UserCoupon.create(
                    1L,
                    rateCoupon,
                    UserCoupon.CouponStatus.ISSUED,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1)
            );

            assertDoesNotThrow(() -> processor.validate(userCoupon, BigDecimal.valueOf(10000)));
        }

        @Test
        @DisplayName("[실패] 최소주문금액 미달이면 예외가 발생한다")
        void failure_validate_whenBelowMinOrder_withRateCoupon() {
            var processor = new CartCouponProcessor();

            var rateCoupon = FixedRateCoupon.builder()
                    .couponCode("CART-RATE-10")
                    .couponName("장바구니 10% 할인 쿠폰")
                    .targetType(Coupon.TargetType.CART)
                    .minOrderAmount(BigDecimal.valueOf(10000))
                    .discountRate(BigDecimal.valueOf(10))
                    .build();

            var userCoupon = UserCoupon.create(
                    1L,
                    rateCoupon,
                    UserCoupon.CouponStatus.ISSUED,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1)
            );

            assertThatThrownBy(() -> processor.validate(userCoupon, BigDecimal.valueOf(9999)))
                    .isInstanceOf(CoreException.class);
        }

         @Test
         @DisplayName("[성공] 최대 할인 금액을 초과하면 최대 할인 금액만큼 할인된다 ")
         void success_apply_respectsMaxDiscountCap_whenPresent() {
             var processor = new CartCouponProcessor();

             var rateCoupon = FixedRateCoupon.builder()
                     .couponCode("CART-RATE-20")
                     .couponName("장바구니 20% 할인 쿠폰")
                     .targetType(Coupon.TargetType.CART)
                     .minOrderAmount(BigDecimal.valueOf(10000))
                     .discountRate(BigDecimal.valueOf(20))
                     .maxDiscountAmount(BigDecimal.valueOf(2000))
                     .build();

             var original = BigDecimal.valueOf(15000);

             var finalPrice = processor.apply(original, rateCoupon);

             assertEquals(BigDecimal.valueOf(13000), finalPrice);
         }
    }
}
