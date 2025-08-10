package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartCouponProcessor implements CouponProcessor {

    @Override
    public Coupon.TargetType getTargetType() {
        return Coupon.TargetType.CART;
    }

    @Override
    public void validate(UserCoupon userCoupon, BigDecimal originalPrice) {
        if (originalPrice.compareTo(userCoupon.getCoupon().getMinOrderAmount()) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "최소 주문 금액을 충족하지 못했습니다.");
        }
    }

    @Override
    public BigDecimal apply(BigDecimal originalPrice, Coupon coupon) {
        return coupon.discount(originalPrice);
    }
}
