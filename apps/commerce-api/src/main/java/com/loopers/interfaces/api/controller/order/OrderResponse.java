package com.loopers.interfaces.api.controller.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderInfo;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public class OrderResponse {

    public record CreateOrder(
            Long orderId,
            Long userId,
            BigDecimal price,
            Order.Status status,
            ZonedDateTime createdAt,
            List<OrderItemResponse> items
    ) {
        public static CreateOrder from(OrderInfo.CreateOrder info) {
            return new CreateOrder(
                    info.orderId(),
                    info.userId(),
                    BigDecimal.valueOf(info.price()),
                    info.status(),
                    info.createdAt(),
                    info.items().stream()
                            .map(OrderItemResponse::from)
                            .toList()
            );
        }
    }

    public record OrderDetail(
            Long orderId,
            Long userId,
            Long price,
            String status,
            ZonedDateTime createdAt,
            List<OrderItemResponse> items
    ) {
        public static OrderDetail from(OrderInfo.OrderDetail info) {
            return new OrderDetail(
                    info.orderId(),
                    info.userId(),
                    info.price(),
                    info.status().name(),
                    info.createdAt(),
                    info.items().stream()
                            .map(OrderItemResponse::from)
                            .toList()
            );
        }
    }

    public record OrderItemResponse(
            Long productSkuId,
            int quantity
    ) {
        public static OrderItemResponse from(OrderInfo.OrderItemInfo info) {
            return new OrderItemResponse(
                    info.productSkuId(),
                    info.quantity()
            );
        }
    }

    public record OrderListItem(
            Long orderId,
            Long userId,
            Long price,
            String status,
            ZonedDateTime createdAt
    ) {
        public static OrderListItem from(OrderInfo.OrderListItem info) {
            return new OrderListItem(
                    info.orderId(),
                    info.userId(),
                    info.price(),
                    info.status().name(),
                    info.createdAt()
            );
        }
    }
}
