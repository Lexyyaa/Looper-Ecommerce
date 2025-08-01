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
}
