package com.loopers.domain.coupon;


import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponProcessorFactory couponProcessorFactory;

    @InjectMocks
    private CouponService couponService;

    @Nested
    @DisplayName("[쿠폰적용]")
    class ApplyCouponsToOrder {

        @Test
        @DisplayName("[실패] cartCouponId로 UserCoupon을 찾지 못하면 NOT_FOUND 예외가 발생한다")
        void failure_applyCouponsToOrder_whenUserCouponNotFound() {
            // arrange
            OrderCommand.CreateOrder command = mock(OrderCommand.CreateOrder.class);
            when(command.cartCouponId()).thenReturn(1L);

            User user = mock(User.class);
            Order order = mock(Order.class);

            when(couponRepository.findByIdWithPessimisticLock(1L))
                    .thenReturn(Optional.empty());

            // act & assert
            CoreException ex = assertThrows(CoreException.class,
                    () -> couponService.applyCartCouponsToOrder(command, user, order));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);

        }

        @Test
        @DisplayName("[실패] 본인 쿠폰 아님 → checkAvailability에서 예외")
        void failure_whenUserIdMismatch() {
            // arrange
            OrderCommand.CreateOrder command = mock(OrderCommand.CreateOrder.class);
            when(command.cartCouponId()).thenReturn(2L);

            User user = mock(User.class);
            when(user.getId()).thenReturn(999L);

            Order order = mock(Order.class);

            UserCoupon userCoupon = mock(UserCoupon.class);
            when(couponRepository.findByIdWithPessimisticLock(2L))
                    .thenReturn(Optional.of(userCoupon));

            CouponProcessor processor = mock(CouponProcessor.class);
            when(couponProcessorFactory.getProcessor(Coupon.TargetType.CART))
                    .thenReturn(processor);

            doThrow(new CoreException(ErrorType.BAD_REQUEST, "본인의 쿠폰이 아닙니다."))
                    .when(userCoupon).checkAvailability(999L);

            // act & assert
            assertThrows(CoreException.class,
                    () -> couponService.applyCartCouponsToOrder(command, user, order));

            verify(processor, never()).validate(any(), any());
        }

        @Test
        @DisplayName("[실패] 최소주문금액 미달 등 validate 실패 시 중단")
        void failure_whenProcessorValidateFails() {
            // arrange
            OrderCommand.CreateOrder command = mock(OrderCommand.CreateOrder.class);
            when(command.cartCouponId()).thenReturn(3L);

            User user = mock(User.class);
            when(user.getId()).thenReturn(1L);

            Order order = mock(Order.class);
            when(order.getPrice()).thenReturn(9000L);

            UserCoupon userCoupon = mock(UserCoupon.class);
            when(couponRepository.findByIdWithPessimisticLock(3L))
                    .thenReturn(Optional.of(userCoupon));

            doNothing().when(userCoupon).checkAvailability(1L);

            CouponProcessor processor = mock(CouponProcessor.class);
            when(couponProcessorFactory.getProcessor(Coupon.TargetType.CART))
                    .thenReturn(processor);

            doThrow(new CoreException(ErrorType.BAD_REQUEST, "최소 주문금액 미달"))
                    .when(processor).validate(eq(userCoupon), any(BigDecimal.class));

            // act & assert
            assertThrows(CoreException.class,
                    () -> couponService.applyCartCouponsToOrder(command, user, order));

            verify(processor, never()).apply(any(), any());
            verify(order, never()).updateFinalPrice(anyLong());
        }
    }
}
