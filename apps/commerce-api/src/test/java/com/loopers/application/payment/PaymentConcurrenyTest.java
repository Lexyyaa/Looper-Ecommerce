package com.loopers.application.payment;

import com.loopers.application.order.OrderApplicationService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSku;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("결제 동시성 테스트")
class PaymentConcurrenyTest {

    @Autowired
    PaymentApplicationService paymentApplicationService;

    @Autowired
    OrderApplicationService orderApplicationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    String loginId = "user1";
    ProductSku sku;
    long initialPoint = 10000L;

    @BeforeEach
    void setUp() {
        userRepository.save(User.create(loginId, User.Gender.M, "사용자1", "2020-02-20", "xx@yy.zz", initialPoint));

        Product p = productRepository.saveProduct(Product.create("맥북프로", Product.Status.ACTIVE, 1L));
        String uniqueSku = "macbookpro-gray-16-" + System.nanoTime();
        sku = productRepository
                .saveProductSkuAndFlush(ProductSku.create(p, uniqueSku, 200,200, 0));
    }

    @AfterEach
    void tearDown(){
        databaseCleanUp.truncateAllTables();
    }


    @DisplayName("[동시성] 동일 유저가 동시에 포인트 결제를 시도해도 잔액은 음수가 되지 않는다")
    @Test
    void concurrency_payment_point_can_not_be_negative() throws InterruptedException {
        int threads = 40;
        long payAmount = 200L;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed  = new AtomicInteger(0);

        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        OrderCommand.CreateOrder orderCmd = new OrderCommand.CreateOrder(
                                loginId,
                                List.of(new OrderCommand.OrderItemCommand(sku.getId(), 1,null)),
                                null
                        );
                        var orderRes = orderApplicationService.order(orderCmd);

                        PaymentCommand.CreatePayment payCmd = new PaymentCommand.CreatePayment(
                                loginId,
                                orderRes.orderId(),
                                payAmount,
                                Payment.Method.POINT,
                                new PaymentCommand.CardPaymentDetails("삼성", "1234-1234-1234-1234")
                        );
                        paymentApplicationService.createPayment(payCmd);
                        success.incrementAndGet();
                    } catch (Exception e) {
                        failed.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        assertThat(success.get()).isBetween(0, 50);

        User after = userRepository.findByLoginId(loginId).orElseThrow();
        assertThat(after.getPoint()).isBetween(0L, initialPoint);
    }

    @Test
    @DisplayName("[동시성] 동일 유저의 서로 다른 주문 동시 결제시 포인트 정확히 차감된다")
    void concurrency_payment_use_correct_point() throws InterruptedException {
        int threads = 40;
        long payAmount = 200L;

        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed  = new AtomicInteger(0);

        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        OrderCommand.CreateOrder orderCmd = new OrderCommand.CreateOrder(
                                loginId,
                                List.of(new OrderCommand.OrderItemCommand(sku.getId(), 1,null)),
                                null
                        );

                        var orderRes = orderApplicationService.order(orderCmd);

                        PaymentCommand.CreatePayment payCmd = new PaymentCommand.CreatePayment(
                                loginId,
                                orderRes.orderId(),
                                payAmount,
                                Payment.Method.POINT,
                                new PaymentCommand.CardPaymentDetails("삼성", "1234-1234-1234-1234")
                        );
                        paymentApplicationService.createPayment(payCmd);
                        success.incrementAndGet();
                    } catch (Exception e) {
                        failed.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        long expectedPoint = initialPoint - success.get() * payAmount;
        User after = userRepository.findByLoginId(loginId).orElseThrow();
        assertThat(after.getPoint()).isEqualTo(expectedPoint);
        assertThat(failed.get()).isBetween(0, threads);
    }
}
