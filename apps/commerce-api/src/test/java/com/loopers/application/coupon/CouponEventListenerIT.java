package com.loopers.application.coupon;


import com.loopers.domain.coupon.CouponUsage;
import com.loopers.domain.coupon.CouponUsedEvent;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.order.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("CouponEventListener")
class CouponEventListenerIT {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PlatformTransactionManager txManager;

    @Test
    @DisplayName("[성공] 커밋 후 쿠폰 사용 처리 및 사용이력 추가")
    void success_after_commit_creates_usage_and_marks_used() {
        UserCoupon userCoupon = Mockito.mock(UserCoupon.class);

        Order order = Mockito.mock(Order.class);
        when(order.getPrice()).thenReturn(10000L);
        when(order.getFinalPrice()).thenReturn(9000L);

        CouponUsedEvent event = Mockito.mock(CouponUsedEvent.class);
        when(event.userCoupon()).thenReturn(userCoupon);
        when(event.order()).thenReturn(order);

        BigDecimal expectedDiscount = BigDecimal.valueOf(1000);
        CouponUsage usage = Mockito.mock(CouponUsage.class);

        try (MockedStatic<CouponUsage> mocked = Mockito.mockStatic(CouponUsage.class)) {
            mocked.when(() -> CouponUsage.create(eq(userCoupon), eq(expectedDiscount)))
                    .thenReturn(usage);

            new TransactionTemplate(txManager).execute(status -> {
                publisher.publishEvent(event);
                return null;
            });

            verify(userCoupon).use();
            mocked.verify(() -> CouponUsage.create(eq(userCoupon), eq(expectedDiscount)));
            verify(order).addCouponUsage(usage);
        }
    }

    @Test
    @DisplayName("[롤백] 롤백되면 리스너 미실행 (use/create/addCouponUsage 호출 없음)")
    void rollback_does_not_invoke_listener() {
        UserCoupon userCoupon = Mockito.mock(UserCoupon.class);

        Order order = Mockito.mock(Order.class);
        when(order.getPrice()).thenReturn(10000L);
        when(order.getFinalPrice()).thenReturn(9000L);

        CouponUsedEvent event = Mockito.mock(CouponUsedEvent.class);
        when(event.userCoupon()).thenReturn(userCoupon);
        when(event.order()).thenReturn(order);

        try (MockedStatic<CouponUsage> mocked = Mockito.mockStatic(CouponUsage.class)) {

            new TransactionTemplate(txManager).execute(status -> {
                publisher.publishEvent(event);
                status.setRollbackOnly();
                return null;
            });

            verifyNoInteractions(userCoupon);
            verify(order, never()).addCouponUsage(any());
            mocked.verifyNoInteractions();
        }
    }
}
