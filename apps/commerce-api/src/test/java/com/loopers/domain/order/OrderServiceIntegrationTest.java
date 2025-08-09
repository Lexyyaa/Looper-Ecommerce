package com.loopers.domain.order;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
class OrderServiceIntegrationTest {

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
    Long productId;
    Long skuId;

    @BeforeEach
    void setUp() {
        User user = User.create("user1", User.Gender.M, "사용자1","2020-02-20","xx@yy.zz",1000L);
        user = userRepository.save(user);
        userId = user.getId();

        Product product = Product.create("맥북프로", Product.Status.ACTIVE,1L);
        product = productRepository.saveProduct(product);
        productId = product.getId();

        ProductSku sku =  ProductSku.create(product, "macbookpro-gray-16",2000, 10, 0);
        sku = productRepository.saveProductSkuAndFlush(sku);
        skuId = sku.getId();
    }

    @AfterEach
    void tearDown(){
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @Transactional
    @DisplayName("[성공] 주문 상세 조회 — 아이템/금액 확인")
    void success_getOrderDetail() {
        Order order = Order.create(userId,0L);
        order.addOrderItem(OrderItem.create(skuId, 3));
        order.updatePrice(2000L * 3);
        order = orderService.saveOrder(order);

        Order found = orderService.getOrder(order.getId());

        assertThat(found.getOrderItems()).hasSize(1);
        assertThat(found.getPrice()).isEqualTo(6000L);
    }

    @Test
    @DisplayName("[성공] 사용자 주문 목록 조회")
    void success_getOrdersByUserId() {
        Order o1 = Order.create(userId,0L);
        o1.addOrderItem(OrderItem.create(skuId, 1));
        o1.updatePrice(2000L);
        orderService.saveOrder(o1);

        Order o2 = Order.create(userId,0L);
        o2.addOrderItem(OrderItem.create(skuId, 2));
        o2.updatePrice(4000L);
        orderService.saveOrder(o2);

        List<Order> list = orderService.getOrdersByUserId(userId);
        assertThat(list).hasSizeGreaterThanOrEqualTo(2);
        assertThat(list.stream().map(Order::getUserId)).allMatch(id -> id.equals(userId));
    }

    @Test
    @DisplayName("[실패] 없는 주문 조회 시 NOT_FOUND")
    void failure_getOrder_whenNotFound() {
        CoreException ex = assertThrows(CoreException.class,
                () -> orderService.getOrder(999999L));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    @DisplayName("[실패] 음수/0 금액으로 업데이트 시 BAD_REQUEST")
    void failure_updatePrice_invalid() {
        Order order = Order.create(userId,0L);
        order.addOrderItem(OrderItem.create(skuId, 1));

        CoreException zero = assertThrows(CoreException.class,
                () -> order.updatePrice(0L));
        assertThat(zero.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

        CoreException negative = assertThrows(CoreException.class,
                () -> order.updatePrice(-100L));
        assertThat(negative.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }


}
