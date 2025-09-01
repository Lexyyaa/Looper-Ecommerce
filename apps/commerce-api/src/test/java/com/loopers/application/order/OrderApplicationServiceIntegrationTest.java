package com.loopers.application.order;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.FixedAmountCoupon;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSku;
import com.loopers.domain.product.ProductSkuService;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("OrderApplicationService 통합 테스트")
class OrderApplicationServiceIntegrationTest {

    @Autowired
    OrderApplicationService orderApplicationService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductSkuService productSkuService;

    @Autowired
    OrderService orderService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    User user;
    String loginId;
    Long userId;
    Long productId;
    Long skuId;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.create("user1", User.Gender.M, "사용자1", "2020-02-20", "xx@yy.zz", 1000L));
        loginId = user.getLoginId();
        userId = user.getId();

        Product product = productRepository.saveProduct(Product.create("맥북프로", Product.Status.ACTIVE, 1L));
        productId = product.getId();

        ProductSku sku = productRepository.saveProductSkuAndFlush(
                ProductSku.create(product, "macbookpro-gray-16", 2000, 10, 0)
        );
        skuId = sku.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

//    @Test
    @DisplayName("[성공] 쿠폰 없이 주문 생성")
    @Transactional
    void success_order_without_coupon() {
        OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                loginId,
                List.of(new OrderCommand.OrderItemCommand(skuId, 2, null)),
                null
        );

        OrderInfo.CreateOrder result = orderApplicationService.order(cmd);

        Order saved = orderService.getOrder(result.orderId());
        assertThat(saved.getPrice()).isEqualTo(4_000L);
        assertThat(saved.getFinalPrice()).isEqualTo(4_000L);
        assertThat(saved.getOrderItems()).hasSize(1);
        assertThat(saved.getOrderItems().get(0).getQuantity()).isEqualTo(2);

        ProductSku after = productSkuService.getBySkuId(skuId);
        assertThat(after.getStockReserved()).isEqualTo(2);
        assertThat(after.getStockTotal()).isEqualTo(10);
    }

//    @Test
    @DisplayName("[성공] 장바구니 정액 2000원 할인 적용")
    @Transactional
    void success_order_with_cart_amount_coupon() {
        FixedAmountCoupon coupon = FixedAmountCoupon.builder()
                .couponCode("CART-AMOUNT-2000")
                .couponName("장바구니 2천원")
                .quantity(100)
                .targetType(Coupon.TargetType.CART)
                .minOrderAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.valueOf(2000))
                .build();
        coupon = (FixedAmountCoupon) couponRepository.save(coupon);

        UserCoupon uc = UserCoupon.create(
                userId, coupon,
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

        OrderInfo.CreateOrder result = orderApplicationService.order(cmd);

        Order saved = orderService.getOrder(result.orderId());
        assertThat(saved.getPrice()).isEqualTo(2000L);
        assertThat(saved.getFinalPrice()).isEqualTo(0L);

        assertThat(saved.getCouponUsages()).hasSize(1);
        assertThat(saved.getCouponUsages().get(0).getUserCoupon().getId()).isEqualTo(uc.getId());
        assertThat(saved.getCouponUsages().get(0).getDiscountedAmount()).isEqualByComparingTo("2000");

        ProductSku after = productSkuService.getBySkuId(skuId);
        assertThat(after.getStockReserved()).isEqualTo(1);
    }

    @Test
    @DisplayName("[실패] 재고 부족 시 BAD_REQUEST")
    void failure_order_insufficient_stock() {
        Product product = productRepository.findBy(productId).orElseThrow();
        ProductSku low = productRepository.saveProductSkuAndFlush(
                ProductSku.create(product, "macbookpro-low-1", 1000, 1, 0)
        );
        Long lowSkuId = low.getId();

        OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                loginId,
                List.of(new OrderCommand.OrderItemCommand(lowSkuId, 2, null)),
                null
        );

        CoreException ex = assertThrows(CoreException.class,
                () -> orderApplicationService.order(cmd));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

        ProductSku after = productSkuService.getBySkuId(lowSkuId);
        assertThat(after.getStockReserved()).isEqualTo(0);
        assertThat(after.getStockTotal()).isEqualTo(1);
    }
}
