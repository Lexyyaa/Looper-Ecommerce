package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CouponProcessorFactory 단위 테스트")
class CouponProcessorFactoryTest {

    @Test
    @DisplayName("[성공] CART 타입에 대해 CartCouponProcessor 를 반환한다")
    void success_getProcessor_whenCart() {
        var cartProcessor = new CartCouponProcessor();
        var factory = new CouponProcessorFactory(List.of(cartProcessor));

        var rateCoupon = FixedRateCoupon.builder()
                .couponCode("CART-RATE-10")
                .couponName("장바구니 10% 할인 쿠폰")
                .targetType(Coupon.TargetType.CART)
                .minOrderAmount(BigDecimal.ZERO)
                .discountRate(BigDecimal.valueOf(10))
                .build();

        var processor = factory.getProcessor(rateCoupon.getTargetType());

        assertThat(processor).isSameAs(cartProcessor);
    }

    @Test
    @DisplayName("[실패] 등록되지 않은 타입이 예외가 발생한다")
    void failure_getProcessor_whenUnsupportedType() {
        var factory = new CouponProcessorFactory(List.of(new CartCouponProcessor()));

        assertThatThrownBy(() -> factory.getProcessor(Coupon.TargetType.PRODUCT))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("해당 쿠폰 타입을 지원하는 Processor가 없습니다.");
    }
}
