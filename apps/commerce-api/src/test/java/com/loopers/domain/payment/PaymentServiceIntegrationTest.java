package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
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
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@ActiveProfiles("PaymentService 통합 테스트")
class PaymentServiceIntegrationTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductSkuService productSkuService;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    Long userId;
    Long orderId;
    Long skuId;

    @BeforeEach
    void setUp() {
        User user = User.create("user1", User.Gender.M, "사용자1","2020-02-20","xx@yy.zz",1000L);
        user = userRepository.save(user);
        userId = user.getId();

        Product product = Product.create("맥북프로", Product.Status.ACTIVE,1L);
        product = productRepository.saveProduct(product);
        ProductSku sku = ProductSku.create(product, "macbookpro-gray-16",3000, 10, 0);
        sku = productRepository.saveProductSkuAndFlush(sku);
        skuId = sku.getId();

        Order order = Order.create(userId,0L);
        order.addOrderItem(OrderItem.create(skuId, 2));
        order.updatePrice(3000L * 2);
        order = orderService.saveOrder(order);
        orderId = order.getId();
    }

    @AfterEach
    void tearDown(){
        databaseCleanUp.truncateAllTables();
    }

    // @Test
    @DisplayName("[성공] 결제요청 시 결제정보 저장 및 상태값 확인")
    void success_pay_point() {
        Payment payment = Payment.create(userId, orderId, 6000L, Payment.Method.POINT);
        Payment paid = paymentService.save(payment);

        assertThat(paid.getMethod()).isEqualTo(Payment.Method.POINT);
        assertThat(paid.getAmount()).isEqualTo(payment.getAmount());
    }

    // @Test
    @DisplayName("[실패] 이미 결제된 주문 재결제 시 CONFLICT")
    void failure_pay_whenDuplicatePayment() {
        Payment payment = Payment.create(userId,orderId, 6000L, Payment.Method.POINT);
        Payment paid = paymentService.save(payment);

        CoreException ex = assertThrows(CoreException.class,
                () -> paymentService.save(paid));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.CONFLICT);
    }
}

