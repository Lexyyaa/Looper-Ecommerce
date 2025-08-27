package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Order")
class OrderEntityTest {

    @Nested
    @DisplayName("[주문 생성 및 항목 추가]")
    class CreateAndAddItems {

        @Test
        @DisplayName("[성공] 주문 생성 시 상태는 PENDING, 가격은 0")
        void success_createOrder() {
            Order order = Order.create(1L, 0L);
            assertThat(order.getStatus()).isEqualTo(Order.Status.PENDING);
            assertThat(order.getPrice()).isZero();
        }

        @Test
        @DisplayName("[성공] 주문 항목 추가 시 OrderItem에 Order 설정됨")
        void success_addOrderItem() {
            Order order = Order.create(1L, 0L);
            OrderItem item = OrderItem.create(10L, 2);

            order.addOrderItem(item);

            assertThat(order.getOrderItems()).containsExactly(item);
            assertThat(item.getOrder()).isEqualTo(order);
        }
    }

    @Nested
    @DisplayName("[주문 취소]")
    class Cancel {

        @Test
        @DisplayName("[성공] PENDING 상태에서 취소하면 상태가 CANCELED")
        void success_cancel() {
            Order order = Order.create(1L, 1000L);
            order.cancel();
            assertThat(order.getStatus()).isEqualTo(Order.Status.CANCELED);
        }

        @Test
        @DisplayName("[실패] 이미 취소된 주문 취소 시 예외 발생")
        void failure_cancelAlreadyCancelled() {
            Order order = Order.create(1L, 1000L);
            order.cancel();
            assertThrows(CoreException.class, order::cancel);
        }
    }

    @Nested
    @DisplayName("[주문 확정]")
    class Confirm {

        @Test
        @DisplayName("[성공] PENDING 상태에서 확정하면 CONFIRMED")
        void success_confirm() {
            Order order = Order.create(1L, 1000L);
            order.confirm();
            assertThat(order.getStatus()).isEqualTo(Order.Status.CONFIRMED);
        }

        @Test
        @DisplayName("[실패] CONFIRMED 상태에서 확정 시 예외")
        void failure_confirmAlreadyConfirmed() {
            Order order = Order.create(1L, 1000L);
            order.confirm();
            assertThrows(CoreException.class, order::confirm);
        }
    }
}
