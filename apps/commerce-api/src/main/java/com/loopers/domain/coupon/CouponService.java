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
    public void applyCouponsToOrder(OrderCommand.CreateOrder command, User user, Order order) {
        if (command.cartCouponId() == null) {
            order.updateFinalPrice(order.getPrice());
            return;
        }

        // UserCoupon 조회
        UserCoupon userCoupon = couponRepository.findByIdWithPessimisticLock(command.cartCouponId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        // 쿠폰 타입에 맞는 Processor 가져옴
        Coupon.TargetType targetType = userCoupon.getCoupon().getTargetType();
        CouponProcessor processor = couponProcessorFactory.getProcessor(targetType);

        // 유효성 검증
        userCoupon.checkAvailability(user.getId());
        BigDecimal originalPrice = BigDecimal.valueOf(order.getPrice());
        processor.validate(userCoupon, originalPrice);

        //쿠폰적용 및 최종결제예정금액 계산
        BigDecimal finalPrice = processor.apply(originalPrice, userCoupon.getCoupon());
        BigDecimal discountedAmount = originalPrice.subtract(finalPrice);

        // 쿠폰 사용 처리
        userCoupon.use();
        couponRepository.save(userCoupon);
        CouponUsage usage = CouponUsage.create(userCoupon, discountedAmount);

        // 주문정보 변경
        order.updateFinalPrice(finalPrice.longValue());
        order.addCouponUsage(usage);
    }
}
