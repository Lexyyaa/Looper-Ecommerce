package com.loopers.domain.order;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderInfo {

    public record CreateOrder(
            Long orderId,
            Long userId,
            Long price,
            Order.Status status,
            ZonedDateTime createdAt,
            List<OrderItemInfo> items
    ) {
        public static CreateOrder from(Order order) {
            return new CreateOrder(
                    order.getId(),
                    order.getUserId(),
                    order.getPrice(),
                    order.getStatus(),
                    order.getCreatedAt(),
                    order.getOrderItems().stream()
                            .map(OrderItemInfo::from)
                            .toList()
            );
        }
    }

    public record OrderItemInfo(
            Long productSkuId,
            int quantity
    ) {
        public static OrderItemInfo from(OrderItem item) {
            return new OrderItemInfo(
                    item.getProductSkuId(),
                    item.getQuantity()
            );
        }
    }

    public record OrderListItem(
            Long orderId,
            Long userId,
            Long price,
            Order.Status status,
            ZonedDateTime createdAt
    ) {
        public static OrderListItem from(Order order) {
            return new OrderListItem(
                    order.getId(),
                    order.getUserId(),
                    order.getPrice(),
                    order.getStatus(),
                    order.getCreatedAt()
            );
        }
    }

    public record OrderDetail(
            Long orderId,
            Long userId,
            Long price,
            com.loopers.domain.order.Order.Status status,
            ZonedDateTime createdAt,
            List<OrderItemInfo> items
    ) {
        public static OrderDetail from(Order order) {
            return new OrderDetail(
                    order.getId(),
                    order.getUserId(),
                    order.getPrice(),
                    order.getStatus(),
                    order.getCreatedAt(),
                    order.getOrderItems().stream()
                            .map(OrderItemInfo::from)
                            .toList()
            );
        }
    }

    public record CancelOrder(
            Long orderId,
            Long userId,
            Order.Status status,
            ZonedDateTime updatedAt
    ) {
        public static CancelOrder from(Order order) {
            return new CancelOrder(
                    order.getId(),
                    order.getUserId(),
                    order.getStatus(),
                    order.getUpdatedAt()
            );
        }
    }
}
