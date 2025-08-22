package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.paymentgateway.PaymentGatewayResponse;
import com.loopers.domain.pg.PgClientAdapter;
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
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"pg.base-url=http://localhost:8080"})
class CardPaymentProcessorTest {

    @Autowired
    private CardPaymentProcessor cardPaymentProcessor;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private PgClientAdapter pgClientAdapter;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private User user;
    private Order order;
    private PaymentCommand.CreatePayment command;

    @BeforeEach
    void setUp() {
        user = User.create("user1", User.Gender.M, "사용자1","2020-02-20","xx@yy.zz",1000L);
        user = userRepository.save(user);

        Product product = Product.create("맥북프로", Product.Status.ACTIVE,1L);
        product = productRepository.saveProduct(product);
        ProductSku sku = ProductSku.create(product, "macbookpro-gray-16",3000, 10, 0);
        sku = productRepository.saveProductSkuAndFlush(sku);

        order = Order.create(user.getId(),0L);
        order.addOrderItem(OrderItem.create(sku.getId(), 2));
        order.updatePrice(3000L * 2);
        order = orderService.saveOrder(order);

        command = new PaymentCommand.CreatePayment(
                user.getLoginId(),
                order.getId(),
                order.getPrice(),
                Payment.Method.CARD,
                new PaymentCommand.CardPaymentDetails("SAMSUNG", "1234-5678-xxxx-xxxx")
        );
    }

    @AfterEach
    void tearDown(){
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("[성공] PG사 결제 요청 성공")
    void success_pg_request() {
        String transactionKey = UUID.randomUUID().toString();
        PaymentGatewayResponse successResponse = new PaymentGatewayResponse(transactionKey, "SUCCESS", null);
        when(pgClientAdapter.acceptPayment(any())).thenReturn(successResponse);

        Payment result = cardPaymentProcessor.pay(user, order, command);

        assertThat(result).isNotNull();
        assertThat(result.getTxKey()).isEqualTo(transactionKey);
        assertThat(result.getStatus()).isEqualTo(Payment.Status.REQUESTED);
    }

    @Test
    @DisplayName("[실패] PG사 결제 요청 실패")
    void failure_pg_request() {
        PaymentGatewayResponse failedResponse = new PaymentGatewayResponse(null, "FAILED", "한도 초과");
        when(pgClientAdapter.acceptPayment(any())).thenReturn(failedResponse);

        Payment result = cardPaymentProcessor.pay(user, order, command);

        Payment paymentInDb = paymentService.getPayment(result.getId());
        assertThat(paymentInDb.getStatus()).isEqualTo(Payment.Status.PENDING);
        assertThat(paymentInDb.getFailReason()).isNull();
    }

    @Test
    @DisplayName("[실패] PG사 통신 오류로 fallback 실행")
    void failure_pg_request_network_error() {
        when(pgClientAdapter.acceptPayment(any())).thenThrow(new RuntimeException("PG사 연결 시간 초과"));

        try {
            Payment result = cardPaymentProcessor.pay(user, order, command);
            Payment paymentInDb = paymentService.getPayment(result.getId());
            assertThat(paymentInDb).isNotNull();
            assertThat(paymentInDb.getStatus()).isEqualTo(Payment.Status.PENDING);
            assertThat(paymentInDb.getFailReason()).contains("PG request failed");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("PG사 연결 시간 초과");
        }
    }
}
