package com.loopers.interfaces.api.controller.order;

import java.util.List;

public class OrderRequest {

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
}
