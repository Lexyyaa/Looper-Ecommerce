package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSku;
import com.loopers.domain.product.ProductSkuService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("PaymentApplicationService 통합 테스트")
class PaymentApplicationServiceIntegrationTest {

    @Autowired
    PaymentApplicationService paymentApplicationService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductSkuService productSkuService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("[성공] 결제 생성(POINT) → Payment=PAID / Order=CONFIRMED")
    @Transactional
    void success_create_payment_point() {
        User user = userRepository.save(User.create("user1", User.Gender.M, "사용자1", "2020-02-20", "xx@yy.zz", 1000L));
        String loginId = user.getLoginId();

        Product product = productRepository.saveProduct(Product.create("맥북프로", Product.Status.ACTIVE, 1L));
        ProductSku sku = productRepository.saveProductSkuAndFlush(
                ProductSku.create(product, "macbookpro-gray-16", 2000, 10, 0)
        );

        Order order = Order.create(user.getId(), 0L);
        productSkuService.reserveStock(sku.getId(), 2);
        order.addOrderItem(OrderItem.create(sku.getId(), 2));
        order.updatePrice(4000L);
        order = orderService.saveOrder(order);

        PaymentCommand.CreatePayment cmd =
                new PaymentCommand.CreatePayment(loginId, order.getId(), 4000L, "POINT");

        PaymentInfo.CreatePayment result = paymentApplicationService.createPayment(cmd);

        Payment savedPay = paymentService.getPayment(result.paymentId());
        assertThat(savedPay.getStatus()).isEqualTo(Payment.Status.PAID);
        assertThat(savedPay.getMethod()).isEqualTo(Payment.Method.POINT);
        assertThat(savedPay.getAmount()).isEqualTo(4000L);
        assertThat(savedPay.getOrderId()).isEqualTo(order.getId());

        Order after = orderService.getOrder(order.getId());
        assertThat(after.getStatus()).isEqualTo(Order.Status.CONFIRMED);
    }

    @Test
    @DisplayName("[실패] 이미 결제한 내역에 대하여 재결제 시 예외 발생")
    void failure_create_payment_already_confirmed() {
        User user = userRepository.save(User.create("user1", User.Gender.M, "사용자1", "2020-02-20", "xx@yy.zz", 1000L));
        String loginId = user.getLoginId();

        Product product = productRepository.saveProduct(Product.create("맥북프로", Product.Status.ACTIVE, 1L));
        ProductSku sku = productRepository.saveProductSkuAndFlush(
                ProductSku.create(product, "macbookpro-gray-16", 3000, 10, 0)
        );

        Order order = Order.create(user.getId(), 0L);
        order.addOrderItem(OrderItem.create(sku.getId(), 1));
        order.updatePrice(3000L);
        order = orderService.saveOrder(order);
        order.confirm();

        PaymentCommand.CreatePayment command =
                new PaymentCommand.CreatePayment(loginId, order.getId(), 3000L, "CREDIT");
        paymentApplicationService.createPayment(command);

        assertThrows(CoreException.class, () -> paymentApplicationService.createPayment(command));
    }


    @Test
    @DisplayName("[실패] 이미 취소된 결제 재취소 시 BAD_REQUEST")
    void failure_cancel_twice_bad_request() {
        User user = userRepository.save(User.create("u4", User.Gender.M, "사용자", "2000-01-01", "u4@u.com", 0L));
        String loginId = user.getLoginId();

        Product product = productRepository.saveProduct(Product.create("키보드", Product.Status.ACTIVE, 1L));
        ProductSku sku = productRepository.saveProductSkuAndFlush(ProductSku.create(product, "sku-4", 5_000, 10, 0));

        Order order = Order.create(user.getId(), 0L);
        productSkuService.reserveStock(sku.getId(), 1);
        order.addOrderItem(OrderItem.create(sku.getId(), 1));
        order.updatePrice(5_000L);
        order = orderService.saveOrder(order);

        PaymentCommand.CreatePayment payCmd = new PaymentCommand.CreatePayment(loginId, order.getId(), 5_000L, "POINT");
        PaymentInfo.CreatePayment payRes = paymentApplicationService.createPayment(payCmd);

        PaymentCommand.CancelPayment cancelCmd = new PaymentCommand.CancelPayment(loginId, order.getId(), payRes.paymentId());

        assertThrows(CoreException.class,
                () -> paymentApplicationService.cancelPayment(cancelCmd));
    }
}
