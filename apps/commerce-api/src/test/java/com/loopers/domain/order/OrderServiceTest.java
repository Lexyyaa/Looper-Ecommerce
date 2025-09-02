package com.loopers.domain.order;


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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("[주문 생성]")
    class SaveOrder {
        @Test
        @DisplayName("[성공] 주문을 생성한다. ")
        void success_saveOrder() {
            Order order = Order.create(1L, 1000L);
            when(orderRepository.save(order)).thenReturn(order);

            Order saved = orderService.saveOrder(order);

            assertThat(saved).isEqualTo(order);
            verify(orderRepository).save(order);
        }
    }

    @Nested
    @DisplayName("[주문 단건 조회]")
    class GetOrder {

//        @Test
        @DisplayName("주문과 함께 주문 항목 목록을 조회한다")
        void success_getOrder_withOrderItems() {
            Order order = Order.create(1L, 1000L);
            OrderItem item1 = OrderItem.create(101L, 2);
            OrderItem item2 = OrderItem.create(102L, 1);
            order.addOrderItem(item1);
            order.addOrderItem(item2);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            Order found = orderService.getOrder(1L);

            assertThat(found.getOrderItems()).hasSize(2);
            assertThat(found.getOrderItems().get(0).getProductSkuId()).isEqualTo(101L);
            assertThat(found.getOrderItems().get(1).getProductSkuId()).isEqualTo(102L);
        }

        @Test
        @DisplayName("[실패] 주문이 존재하지 않으면 NOT_FOUND 에러를 반환한다. ")
        void failure_orderNotFound() {
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            CoreException ex = assertThrows(CoreException.class,
                    () -> orderService.getOrder(1L));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("[주문 목록 조회]")
    class GetOrdersByUser {
        @Test
        @DisplayName("[성공] 해당 사용자의 주문목록을 조회한다. ")
        void success_getOrdersByUser() {
            Order order = Order.create(1L, 1000L);
            when(orderRepository.findByUserId(1L)).thenReturn(List.of(order));

            List<Order> result = orderService.getOrdersByUserId(1L);

            assertThat(result).hasSize(1);
        }
    }
}
