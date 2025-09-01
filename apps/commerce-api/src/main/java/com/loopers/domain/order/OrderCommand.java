package com.loopers.domain.order;

import java.util.List;

public class OrderCommand {

    public record CreateOrder(
            String loginId,
            List<OrderItemCommand> items,
            Long cartCouponId
    ) {
        public boolean hasCoupon() {
            return cartCouponId != null;
        }

        public List<Long> getProductSkuIds() {
            return items.stream().map(OrderItemCommand::productSkuId).toList();
        }
    }

    public record OrderItemCommand(
            Long productSkuId,
            int quantity,
            Long itemCouponId
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




