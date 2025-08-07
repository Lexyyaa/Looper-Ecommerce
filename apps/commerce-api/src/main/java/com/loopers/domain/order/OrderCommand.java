package com.loopers.domain.order;

import java.util.List;

public class OrderCommand {

    public record CreateOrder(
            String loginId,
            List<OrderItemCommand> items
    ) {

    }

    public record OrderItemCommand(
            Long productSkuId,
            int quantity
    ) {}

    public record CancelOrder(
            String loginId,
            Long orderId
    ) {}

    public record OrderDetail(
            String loginId,
            Long orderId
    ) {}
}
