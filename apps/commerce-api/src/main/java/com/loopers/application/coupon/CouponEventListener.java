package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponUsage;
import com.loopers.domain.coupon.CouponUsedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CouponEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCouponUsed(CouponUsedEvent e) {
        e.userCoupon().use();
        CouponUsage usage = CouponUsage.create(e.userCoupon(), BigDecimal.valueOf(e.order().getPrice() - e.order().getFinalPrice()));
        e.order().addCouponUsage(usage);
    }
}
