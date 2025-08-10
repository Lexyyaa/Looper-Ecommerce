package com.loopers.domain.coupon;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSku;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("CouponService 통합 테스트 ")
class CouponServiceIntegrationTest {

    @Autowired
    CouponService couponService;
    @Autowired
    CouponRepository couponRepository;
    @Autowired
    OrderService orderService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    DatabaseCleanUp databaseCleanUp;

    User user;
    String loginId;
    Long userId;
    Long skuId;

    @BeforeEach
    void setUp() {
        User user = User.create("user1", User.Gender.M, "사용자1","2020-02-20","xx@yy.zz",1000L);
        user = userRepository.save(user);
        userId = user.getId();
        loginId = user.getLoginId();

        Product product = Product.create("맥북프로", Product.Status.ACTIVE,1L);
        product = productRepository.saveProduct(product);

        ProductSku sku =  ProductSku.create(product, "macbookpro-gray-16",2000, 10, 0);
        sku = productRepository.saveProductSkuAndFlush(sku);
        skuId = sku.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("[성공] 쿠폰 없이 주문을 생성하면 최종 결제 금액이 원가와 동일하다")
    void success_apply_no_coupon() {
        Order order = Order.create(userId, 0L);
        order.addOrderItem(OrderItem.create(skuId, 1));
        order.updatePrice(10000L);
        order = orderService.saveOrder(order);

        OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                loginId,
                List.of(
                        new OrderCommand.OrderItemCommand(skuId, 1, null)
                ),
                null
        );

        couponService.applyCouponsToOrder(cmd, user, order);

        assertThat(order.getFinalPrice()).isEqualTo(10000L);
        assertThat(order.getCouponUsages()).isEmpty();
    }

    @Test
    @DisplayName("[성공] 장바구니 정액 2,000원 할인 쿠폰을 적용하면 최종 결제 금액이 18000원이 되고 사용 내역이 기록된다")
    void success_apply_cart_amount_coupon() {

        User user1 = User.create("user1", User.Gender.M, "사용자1","2020-02-20","xx@yy.zz",1000L);
        User currUser = userRepository.save(user1);

        Order order = Order.create(currUser.getId(), 0L);
        order.addOrderItem(OrderItem.create(skuId, 1));
        order.updatePrice(20000L);
        order = orderService.saveOrder(order);

        FixedAmountCoupon coupon = FixedAmountCoupon.builder()
                .couponCode("CART-AMOUNT-2000")
                .couponName("장바구니 2천원 할인 쿠폰")
                .targetType(Coupon.TargetType.CART)
                .minOrderAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.valueOf(2000))
                .build();

        coupon = (FixedAmountCoupon) couponRepository.save(coupon);

        UserCoupon uc = UserCoupon.create(currUser.getId(),
                coupon,
                UserCoupon.CouponStatus.ISSUED,
                LocalDateTime.now(),
                LocalDateTime.now().plusWeeks(1));

        uc = couponRepository.save(uc);

        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder(
                loginId,
                List.of(new OrderCommand.OrderItemCommand(skuId, 1, null)),
                uc.getId()
        );

        couponService.applyCouponsToOrder(command, currUser, order);

        assertThat(order.getFinalPrice()).isEqualTo(18000L);
        assertThat(order.getCouponUsages()).hasSize(1);
        assertThat(order.getCouponUsages().get(0).getDiscountedAmount()).isEqualByComparingTo("2000");
        assertThat(order.getCouponUsages().get(0).getUserCoupon().getId()).isEqualTo(uc.getId());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 쿠폰 ID로 쿠폰을 적용하면 NOT_FOUND 예외가 발생한다")
    void failure_coupon_not_found() {
        Order order = Order.create(userId, 0L);
        order.addOrderItem(OrderItem.create(skuId, 1));
        order.updatePrice(10000L);
        Order currOrder = orderService.saveOrder(order);

        OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                loginId,
                List.of(new OrderCommand.OrderItemCommand(skuId, 1, null)),
                999L
        );

        CoreException ex = assertThrows(CoreException.class,
                () -> couponService.applyCouponsToOrder(cmd, user, currOrder));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    @DisplayName("[실패] 최소 주문 금액에 미달하는 경우 BAD_REQUEST 예외가 발생한다")
    void failure_min_order_amount() {

        User user1 = User.create("user1", User.Gender.M, "사용자1","2020-02-20","xx@yy.zz",1000L);
        User currUser = userRepository.save(user1);


        Order order = Order.create(userId, 0L);
        order.addOrderItem(OrderItem.create(skuId, 1));
        order.updatePrice(3000L);

        Order currOrder = orderService.saveOrder(order);

        FixedAmountCoupon coupon = FixedAmountCoupon.builder()
                .couponCode("CART-AMOUNT-2000-MIN5000")
                .couponName("장바구니 2천원 할인 쿠폰(최소5천)")
                .targetType(Coupon.TargetType.CART)
                .minOrderAmount(BigDecimal.ZERO)
                .quantity(100)
                .build();
        coupon = (FixedAmountCoupon) couponRepository.save(coupon);

        UserCoupon uc = UserCoupon.create(
                userId,
                coupon,
                UserCoupon.CouponStatus.ISSUED,
                LocalDateTime.now(),
                LocalDateTime.now().plusWeeks(1)
        );
        uc = couponRepository.save(uc);

        OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                loginId,
                List.of(new OrderCommand.OrderItemCommand(skuId, 1, null)),
                uc.getId()
        );

        CoreException ex = assertThrows(CoreException.class,
                () -> couponService.applyCouponsToOrder(cmd, currUser, currOrder));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(uc.getStatus()).isEqualTo(UserCoupon.CouponStatus.ISSUED);
    }
}
