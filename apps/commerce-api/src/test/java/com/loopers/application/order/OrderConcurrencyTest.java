package com.loopers.application.order;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.FixedAmountCoupon;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderInfo;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("주문 동시성 테스트")
class OrderConcurrencyTest {

    @Autowired
    OrderApplicationService orderApplicationService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductSkuService productSkuService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CouponRepository couponRepository;
    @Autowired
    DatabaseCleanUp databaseCleanUp;

    String loginId;
    Long skuId;
    Long userCouponId;

    @BeforeEach
    void setUp() {
        User u  = userRepository.save(User.create("user1", User.Gender.M, "사용자1", "2020-02-20", "xx@yy.zz", 10000L));

        loginId = u.getLoginId();

        Product p = productRepository.saveProduct(Product.create("맥북프로", Product.Status.ACTIVE, 1L));
        ProductSku sku = ProductSku.create(p, "macbookpro-gray-16", 2000, 100, 0);
        skuId = productRepository.saveProductSkuAndFlush(sku).getId();

        FixedAmountCoupon coupon = FixedAmountCoupon.builder()
                .couponCode("CART-AMOUNT-500")
                .couponName("장바구니 500원")
                .quantity(1)
                .targetType(Coupon.TargetType.CART)
                .minOrderAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.valueOf(500))
                .build();
        coupon = (FixedAmountCoupon) couponRepository.save(coupon);

        UserCoupon uc = UserCoupon.create(
                u.getId(),
                coupon,
                UserCoupon.CouponStatus.ISSUED,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1)
        );
        userCouponId = couponRepository.save(uc).getId();
    }

    @AfterEach
    void tearDown(){
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("[동시성] 재고가 충분해도 100명 중 1명만 성공하도록 낙관적 락 충돌 유도")
    void stock_bounded_orders() throws InterruptedException {
        int threads = 100;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                                loginId,
                                List.of(new OrderCommand.OrderItemCommand(skuId, 1, null)),
                                null
                        );
                        orderApplicationService.order(cmd);
                        success.incrementAndGet();
                    }  catch (Exception e) {
                            failed.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        } finally {
            executor.shutdownNow();
        }

        assertThat(success.get()).isEqualTo(1);
        assertThat(failed.get()).isEqualTo(threads - 1);
    }

    @Test
    @DisplayName("[동시성] 1명의 사용자가 동일 쿠폰을 동시에 여러 번 사용하면 한 번만 성공한다")
    void single_coupon_used_once() throws InterruptedException {
        int threads = 100;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                                loginId,
                                List.of(new OrderCommand.OrderItemCommand(skuId, 1, null)),
                                userCouponId
                        );
                        OrderInfo.CreateOrder res = orderApplicationService.order(cmd);
                        assertThat(res.finalPrice()).isEqualTo(1500L);
                        success.incrementAndGet();
                    } catch (CoreException e) {
                        failed.incrementAndGet();
                    } catch (Exception e) {
                        failed.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        } finally {
            executor.shutdownNow();
        }

        assertThat(success.get()).isEqualTo(1);
        assertThat(failed.get()).isEqualTo(threads - 1);
    }
}
