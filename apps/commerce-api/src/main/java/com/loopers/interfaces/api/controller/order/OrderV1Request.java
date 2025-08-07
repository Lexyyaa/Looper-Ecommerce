package com.loopers.interfaces.api.controller.order;

import java.util.List;

public class OrderV1Request {

    public record CreateOrder(
            String loginId,
            List<OrderItem> items
    ) {

    }
    public record OrderItem(
            Long productSkuId,
            int quantity
    ) {
    }

    public record CancelOrder(
            String loginId,
            Long orderId
    ) {}
}
