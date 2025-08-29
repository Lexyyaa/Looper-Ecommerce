package com.loopers.application.order;

import com.loopers.domain.coupon.CouponProcessorFactory;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.UserCouponService;
import com.loopers.domain.monitoring.activity.ActivityPublisher;
import com.loopers.domain.monitoring.activity.payload.OrderActivityPayload;
import com.loopers.domain.monitoring.resultlog.ResultLogPayload;
import com.loopers.domain.monitoring.resultlog.ResultLogPublisher;
import com.loopers.domain.monitoring.resultlog.payload.OrderResultLogs;
import com.loopers.domain.order.*;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductSku;
import com.loopers.domain.product.ProductSkuService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.shared.logging.Envelope;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderApplicationService")
class OrderApplicationServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private ProductSkuService productSkuService;
    @Mock
    private ProductService productService;
    @Mock
    private OrderService orderService;
    @Mock
    private ActivityPublisher activityPublisher;
    @Mock
    private OrderEventPublisher orderEventPublisher;
    @Mock
    ResultLogPublisher resultLogPublisher;
    @InjectMocks
    private OrderApplicationService orderApplicationService;

    @Nested
    @DisplayName("[주문 생성]")
    class CreateOrder {
//        @Test
        @DisplayName("[성공] 정상적으로 주문을 생성한다.")
        void success_createOrder() {
            // Arrange
            OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                    "loginId",
                    List.of(new OrderCommand.OrderItemCommand(10L, 2, null)),
                    1L
            );

            User user = User.builder().id(1L).build();

            ProductSku sku = ProductSku.builder()
                    .id(10L)
                    .price(1000)
                    .stockTotal(2)
                    .stockReserved(0)
                    .product(Product.builder().id(100L).status(Product.Status.ACTIVE).build())
                    .build();

            when(userService.getUser("loginId")).thenReturn(user);
            when(productSkuService.getBySkuId(10L)).thenReturn(sku);

            Order persisted = Order.builder()
                    .userId(user.getId())
                    .price(2000L)
                    .build();
            when(orderService.saveOrder(any(Order.class))).thenReturn(persisted);

            // Act
            OrderInfo.CreateOrder result = orderApplicationService.order(cmd);

            verify(activityPublisher).publish(argThat((Envelope<?> e) ->
                    e.actorId().equals("loginId")
                            && e.payload() instanceof OrderActivityPayload.OrderRequested p
                            && p.skuIds().equals(List.of(10L))
            ));

            verify(productSkuService).reserveStock(10L, 2);
            verify(orderService).saveOrder(argThat(o ->
                    o.getId() == null
                            && o.getUserId().equals(1L)
                            && o.getOrderItems().size() == 1
                            && o.getOrderItems().get(0).getProductSkuId().equals(10L)
                            && o.getPrice().equals(2000L)
            ));

            verify(orderEventPublisher).reCalStock(argThat(ev ->
                    ev instanceof OrderEvent.ReCalStock r
                            && r.order().getUserId().equals(1L)
                            && r.order().getOrderItems().size() == 1
                            && r.order().getOrderItems().get(0).getProductSkuId().equals(10L)
                            && r.order().getPrice().equals(2000L)
            ));

            verify(resultLogPublisher).publish((Envelope<? extends ResultLogPayload>) argThat((Envelope<?> e) ->
                    e.actorId().equals("loginId")
                            && e.payload() instanceof OrderResultLogs.OrderSucceeded p
            ));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자일 경우 NOT_FOUND 에러를 반환한다.")
        void failure_userNotFound() {
            OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                    "loginId",
                    List.of(new OrderCommand.OrderItemCommand(10L, 2,null)),
                    1L
            );
            when(userService.getUser("loginId"))
                    .thenThrow(new CoreException(ErrorType.NOT_FOUND, "사용자 없음"));

            CoreException ex = assertThrows(CoreException.class,
                    () -> orderApplicationService.order(cmd));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 상품 옵션 NOT_FOUND 에러를 반환한다.")
        void failure_skuNotFound() {
            OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                    "loginId",
                    List.of(new OrderCommand.OrderItemCommand(10L, 2,null)),
                    1L
            );
            User user = User.builder().id(1L).build();
            when(userService.getUser("loginId")).thenReturn(user);
            when(productSkuService.getBySkuId(10L))
                    .thenThrow(new CoreException(ErrorType.NOT_FOUND, "옵션 없음"));

            CoreException ex = assertThrows(CoreException.class,
                    () -> orderApplicationService.order(cmd));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

//        @Test
        @DisplayName("[실패] 재고 부족할 경우 BAD_REQUEST 에러를 반환한다.")
        void failure_insufficientStock() {
            OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                    "loginId",
                    List.of(new OrderCommand.OrderItemCommand(10L, 2,null)),
                    1L
            );
            User user = User.builder().id(1L).build();
            ProductSku sku = ProductSku.builder().id(10L).build();
            when(userService.getUser("loginId")).thenReturn(user);
            when(productSkuService.getBySkuId(10L)).thenReturn(sku);
            doThrow(new CoreException(ErrorType.BAD_REQUEST, "재고 부족"))
                    .when(productSkuService).reserveStock(sku.getId(), 5);

            CoreException ex = assertThrows(CoreException.class,
                    () -> orderApplicationService.order(cmd));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

//        @Test
        @DisplayName("[성공] 모든 옵션이 품절이면 상품 상태를 SOLD_OUT으로 변경한다")
        void success_updateProductStatusToSoldOut() {
            OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                    "loginId",
                    List.of(new OrderCommand.OrderItemCommand(10L, 2,null)),
                    1L
            );

            User user = User.builder().id(1L).build();

            ProductSku sku = ProductSku.builder()
                    .id(10L)
                    .price(1000)
                    .stockTotal(2)
                    .stockReserved(0)
                    .product(Product.builder().id(100L).status(Product.Status.ACTIVE).build())
                    .build();

            Order order = Order.create(user.getId(), 2000L);

            when(userService.getUser("loginId")).thenReturn(user);
            when(productSkuService.getBySkuId(10L)).thenReturn(sku);
            when(productSkuService.isAllSoldOut(100L)).thenReturn(true);
            when(orderService.saveOrder(any(Order.class))).thenReturn(order);

            orderApplicationService.order(cmd);

            verify(productService).updateStatus(true, 100L);
        }

        //       @Test
        @DisplayName("[성공] 일부 옵션만 품절이면 상품 상태를 변경하지 않는다")
        void success_doNotUpdateStatusWhenNotAllSoldOut() {
            OrderCommand.CreateOrder cmd = new OrderCommand.CreateOrder(
                    "loginId",
                    List.of(new OrderCommand.OrderItemCommand(10L, 2,null)),
                    1L
            );
            User user = User.builder().id(1L).build();

            ProductSku sku = ProductSku.builder()
                    .id(10L)
                    .price(1000)
                    .stockTotal(5)
                    .stockReserved(0)
                    .product(Product.builder().id(100L).status(Product.Status.ACTIVE).build())
                    .build();

            Order order = Order.create(user.getId(), 2000L);

            when(userService.getUser("loginId")).thenReturn(user);
            when(productSkuService.getBySkuId(10L)).thenReturn(sku);
            when(productSkuService.isAllSoldOut(100L)).thenReturn(false);
            when(orderService.saveOrder(any(Order.class))).thenReturn(order);

            orderApplicationService.order(cmd);

            verify(productService, never()).updateStatus(true, 100L);
        }
    }

    @Nested
    @DisplayName("[주문 목록 조회]")
    class GetOrders {
        @Test
        @DisplayName("[성공] 사용자 주문 목록을 조회한다. ")
        void success_getOrders() {
            User user = User.builder().id(1L).build();
            Order order = Order.create(user.getId(), 1000L);
            when(userService.getUser("loginId")).thenReturn(user);
            when(orderService.getOrdersByUserId(1L)).thenReturn(List.of(order));

            var result = orderApplicationService.getOrdersByUserId("loginId");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("[주문 상세 조회]")
    class GetOrderDetail {
        @Test
        @DisplayName("[성공] 주문에 대한 상세정보를 조회한다. ")
        void success_detail() {
            Order order = Order.create(1L, 1000L);
            when(orderService.getOrder(1L)).thenReturn(order);

            var detail = orderApplicationService.getOrderDetail(1L);

            assertThat(detail.price()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("주문 상세 조회 시 주문 항목 목록이 포함된다")
        void success_detail_withOrderItems() {
            Order order = Order.create(1L, 1000L);
            order.addOrderItem(OrderItem.create(101L, 2));
            order.addOrderItem(OrderItem.create(102L, 1));

            when(orderService.getOrder(1L)).thenReturn(order);

            var detail = orderApplicationService.getOrderDetail(1L);

            assertThat(detail.items()).hasSize(2);
            assertThat(detail.items().get(0).productSkuId()).isEqualTo(101L);
            assertThat(detail.items().get(1).productSkuId()).isEqualTo(102L);
        }

        @Test
        @DisplayName("[실패] 해당 주문이 없을경우 NOT_FOUND 에러를 반환한다. ")
        void failure_notFound() {
            when(orderService.getOrder(1L))
                    .thenThrow(new CoreException(ErrorType.NOT_FOUND, "주문 없음"));

            CoreException ex = assertThrows(CoreException.class,
                    () -> orderApplicationService.getOrderDetail(1L));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
