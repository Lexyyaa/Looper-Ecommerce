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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        @DisplayName("[성공] 적용된쿠폰이 없다면 주문총액을 최종결제금액으로 설정한다")
        void success_applyCouponsToOrder_whenCartCouponIdIsNull() {
            // arrange
            OrderCommand.CreateOrder command = mock(OrderCommand.CreateOrder.class);
            when(command.cartCouponId()).thenReturn(null);

            User user = mock(User.class);
            Order order = mock(Order.class);
            when(order.getPrice()).thenReturn(50000L);

            // act
            couponService.applyCouponsToOrder(command, user, order);

            // assert
            verify(order).updateFinalPrice(50000L);
            verifyNoInteractions(couponRepository, couponProcessorFactory);
        }

        @Test
        @DisplayName("[실패] cartCouponId로 UserCoupon을 찾지 못하면 NOT_FOUND 예외가 발생한다")
        void failure_applyCouponsToOrder_whenUserCouponNotFound() {
            // arrange
            OrderCommand.CreateOrder command = mock(OrderCommand.CreateOrder.class);
            when(command.cartCouponId()).thenReturn(1L);

            when(couponRepository.findByUserCouponId(1L)).thenReturn(Optional.empty());

            User user = mock(User.class);
            Order order = mock(Order.class);

            // act & assert
            assertThatThrownBy(() -> couponService.applyCouponsToOrder(command, user, order))
                    .isInstanceOf(CoreException.class)
                    .extracting(ex -> ((CoreException) ex).getErrorType())
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(couponRepository, never()).save(any(CouponUsage.class));
            verify(order, never()).updateFinalPrice(anyLong());
        }

        @Test
        @DisplayName("[실패] 본인의 쿠폰이 아니면 checkAvailability에서 예외가 발생한다")
        void failure_applyCouponsToOrder_whenUserIdMismatch() {
            // arrange
            OrderCommand.CreateOrder command = mock(OrderCommand.CreateOrder.class);
            when(command.cartCouponId()).thenReturn(1L);

            FixedAmountCoupon coupon = FixedAmountCoupon.builder()
                    .couponCode("CART-AMOUNT-1000")
                    .couponName("장바구니 천원 할인 쿠폰")
                    .targetType(Coupon.TargetType.CART)
                    .minOrderAmount(BigDecimal.ZERO)
                    .discountAmount(BigDecimal.valueOf(1000))
                    .build();

            UserCoupon userCoupon = UserCoupon.create(
                    2L,
                    coupon,
                    UserCoupon.CouponStatus.ISSUED,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1)
            );
            when(couponRepository.findByUserCouponId(1L)).thenReturn(Optional.of(userCoupon));

            CouponProcessor processor = mock(CouponProcessor.class);
            when(couponProcessorFactory.getProcessor(Coupon.TargetType.CART)).thenReturn(processor);

            User user = mock(User.class);
            when(user.getId()).thenReturn(3L);

            Order order = mock(Order.class);

            // act & assert
            assertThatThrownBy(() -> couponService.applyCouponsToOrder(command, user, order))
                    .isInstanceOf(CoreException.class)
                    .extracting(ex -> ((CoreException) ex).getErrorType())
                    .isEqualTo(ErrorType.BAD_REQUEST);

            verify(couponProcessorFactory).getProcessor(Coupon.TargetType.CART);
            verify(order, never()).updateFinalPrice(anyLong());
        }

        @Test
        @DisplayName("[실패] 최소주문금액 미달 등으로 예외가 발생하면 중단한다")
        void failure_applyCouponsToOrder_whenProcessorValidateFails() {
            // arrange
            OrderCommand.CreateOrder command = mock(OrderCommand.CreateOrder.class);
            when(command.cartCouponId()).thenReturn(1L);

            FixedAmountCoupon coupon = FixedAmountCoupon.builder()
                    .couponCode("CART-AMOUNT-3000")
                    .couponName("장바구니 3천원 할인 쿠폰")
                    .targetType(Coupon.TargetType.CART)
                    .minOrderAmount(BigDecimal.valueOf(50000))
                    .discountAmount(BigDecimal.valueOf(3000))
                    .build();

            UserCoupon userCoupon = UserCoupon.create(
                    10L,
                    coupon,
                    UserCoupon.CouponStatus.ISSUED,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1)
            );
            when(couponRepository.findByUserCouponId(1L)).thenReturn(Optional.of(userCoupon));

            CouponProcessor processor = mock(CouponProcessor.class);
            when(couponProcessorFactory.getProcessor(Coupon.TargetType.CART)).thenReturn(processor);

            User user = mock(User.class);
            when(user.getId()).thenReturn(10L);

            Order order = mock(Order.class);
            when(order.getPrice()).thenReturn(40000L);

            doThrow(new CoreException(ErrorType.BAD_REQUEST, "최소 주문 금액을 충족하지 못했습니다."))
                    .when(processor).validate(eq(userCoupon), eq(BigDecimal.valueOf(40000L)));

            // act & assert
            assertThatThrownBy(() -> couponService.applyCouponsToOrder(command, user, order))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("최소 주문 금액");

            verify(couponRepository, never()).save(any(CouponUsage.class));
        }

        @Test
        @DisplayName("[성공] 유효한 장바구니 쿠폰이면 할인 적용,사용내역 저장,최종결제금액 게산,쿠폰 사용 처리를 진행한다")
        void success_applyCouponsToOrder_whenValidCartCoupon() {
            // arrange
            OrderCommand.CreateOrder command = mock(OrderCommand.CreateOrder.class);
            when(command.cartCouponId()).thenReturn(1L);

            FixedAmountCoupon coupon = FixedAmountCoupon.builder()
                    .couponCode("CART-AMOUNT-3000")
                    .couponName("장바구니 3천원 할인 쿠폰")
                    .targetType(Coupon.TargetType.CART)
                    .minOrderAmount(BigDecimal.ZERO)
                    .discountAmount(BigDecimal.valueOf(3000))
                    .build();

            UserCoupon userCoupon = UserCoupon.create(
                    10L,
                    coupon,
                    UserCoupon.CouponStatus.ISSUED,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1)
            );
            when(couponRepository.findByUserCouponId(1L)).thenReturn(Optional.of(userCoupon));

            CouponProcessor processor = mock(CouponProcessor.class);
            when(couponProcessorFactory.getProcessor(Coupon.TargetType.CART)).thenReturn(processor);

            User user = mock(User.class);
            when(user.getId()).thenReturn(10L);

            Order order = mock(Order.class);
            when(order.getPrice()).thenReturn(20000L);

            // validate는 예외 없음
            BigDecimal original = BigDecimal.valueOf(20000);
            BigDecimal finalPrice = BigDecimal.valueOf(17000);
            when(processor.apply(original, coupon)).thenReturn(finalPrice);

            // act
            couponService.applyCouponsToOrder(command, user, order);

            // assert: 프로세서 호출
            verify(processor).validate(eq(userCoupon), eq(original));
            verify(processor).apply(eq(original), eq(coupon));

            // 사용 내역 저장
            verify(couponRepository).save(any(CouponUsage.class));

            // 주문 업데이트
            verify(order).updateFinalPrice(17000L);
            verify(order).addCouponUsage(any(CouponUsage.class));

            // 쿠폰 상태 변경
            assertEquals(UserCoupon.CouponStatus.USED, userCoupon.getStatus());
            assertNotNull(userCoupon.getUsedAt());
        }
    }
}
