package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.*;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductSku;
import com.loopers.domain.product.ProductSkuService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderApplicationService implements OrderUsecase {

    private final UserService userService;
    private final ProductSkuService productSkuService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;

    @Override
    @Transactional
    public OrderInfo.CreateOrder order(OrderCommand.CreateOrder command) {

        User user = userService.getUser(command.loginId());

        // 주문 생성 및 주문 총액 계산 (PENDING 으로 생성)
        Order order = Order.create(user.getId(),0L);
        BigDecimal initialPrice = BigDecimal.ZERO;

        // 주문목록에 각각에 대하여 재고선점 및 주문추가
        for (OrderCommand.OrderItemCommand itemCommand : command.items()) {
            // 상품SKU 조회
            ProductSku sku = productSkuService.getBySkuId(itemCommand.productSkuId());
            // 상품 재고선점, 재고차감
            productSkuService.reserveStock(sku.getId(), itemCommand.quantity());
            // 해당 상품에 대한 주문총액 계산
            initialPrice = initialPrice.add(Order.getInitialTotalPrice(sku.getPrice(),itemCommand.quantity()));
            // 상품쿠폰이 있을 경우 해당 위치에 로직 추가
            // 주문목록에 추가
            order.addOrderItem(OrderItem.create(itemCommand.productSkuId(), itemCommand.quantity()));
        }
        // 주문서에 총 주문금액 추가
        order.updatePrice(initialPrice.longValue());
        // 쿠폰 적용
        couponService.applyCartCouponsToOrder(command, user, order);

        // 주문 저장
        Order savedOrder = orderService.saveOrder(order);

        // 상품 품절여부 확인 및 업데이트
        command.items().forEach(item -> {
            ProductSku sku = productSkuService.getBySkuId(item.productSkuId());
            boolean isAllSoldOut = productSkuService.isAllSoldOut(sku.getProduct().getId());
            productService.updateStatus(isAllSoldOut, sku.getProduct().getId());
        });

        return OrderInfo.CreateOrder.from(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderInfo.OrderListItem> getOrdersByUserId(String loginId) {
        // 사용자조회
        User user = userService.getUser(loginId);

        return orderService.getOrdersByUserId(user.getId())
                .stream()
                .map(OrderInfo.OrderListItem::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderInfo.OrderDetail getOrderDetail(Long orderId) {
        Order order = orderService.getOrder(orderId);
        return OrderInfo.OrderDetail.from(order);
    }

    @Override
    @Transactional
    public OrderInfo.CancelOrder cancelOrder(OrderCommand.CancelOrder command) {
        // 사용자 조회
        User user = userService.getUser(command.loginId());
        // 취소 가능 주문 조회
        Order order = orderService.getCancelableOrder(command.orderId(),user.getId());
        // 재고 복구
        order.getOrderItems().forEach(item ->
                productSkuService.rollbackReservedStock(item.getProductSkuId(), item.getQuantity())
        );
        // 주문 상태 변경 및 저장
        orderService.cancelOrder(order);
        return OrderInfo.CancelOrder.from(order);
    }
}
