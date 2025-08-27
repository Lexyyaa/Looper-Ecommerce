package com.loopers.interfaces.api.controller.order;

import java.util.List;

public class OrderV1Request {

    public record CreateOrder(
            String loginId,
            List<OrderItemRequest> items, // 여러개의 주문을 담기위해 리스트로 선언 // 만약에 상품쿠폰있으면 여기서 처리
            Long cartCouponId // 장바구니 쿠폰
    ) {

    }
    public record OrderItemRequest(
            Long productSkuId,
            int quantity,
            Long itemCouponId // 상품쿠폰
    ) {
    }
}
