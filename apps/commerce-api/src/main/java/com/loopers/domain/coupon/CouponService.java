package com.loopers.domain.coupon;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponProcessorFactory couponProcessorFactory;


    @Transactional
    public UserCoupon applyCartCouponsToOrder(OrderCommand.CreateOrder command, User user, Order order) {

        // 발급된 사용자쿠폰 조회
        UserCoupon userCoupon = couponRepository.findByIdWithPessimisticLock(command.cartCouponId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        // 쿠폰 타입에 맞는 Processor 가져옴
        CouponProcessor processor = couponProcessorFactory.getProcessor(Coupon.TargetType.CART);

        // 쿠폰상태 및 만료에 대한 유효성 검사
        userCoupon.checkAvailability(user.getId());

        // 쿠폰별 사용가능 조건에 대한 검사
        BigDecimal originalPrice = BigDecimal.valueOf(order.getPrice());
        processor.validate(userCoupon, originalPrice);

        //쿠폰적용 및 최종결제예정금액 계산
        BigDecimal finalPrice = processor.apply(originalPrice, userCoupon.getCoupon());

        // 최종 결제금액 변경
        order.updateFinalPrice(finalPrice.longValue());
        return userCoupon;
    }
}
