package com.loopers.interfaces.api.controller.order;

import java.util.List;

public class OrderV1Request {

    public record CreateOrder(
            String loginId,
            List<OrderItemRequest> items,
            Long cartCouponId
    ) {

    }
    public record OrderItemRequest(
            Long productSkuId,
            int quantity,
            Long itemCouponId
    ) {
    }

    public record CancelOrder(
            String loginId,
            Long orderId
    ) {}
}
