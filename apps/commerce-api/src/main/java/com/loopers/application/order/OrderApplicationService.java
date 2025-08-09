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

        // 주문 생성 및 주문 총액 계산
        Order order = Order.create(user.getId(),0L);
        BigDecimal initialPrice = BigDecimal.ZERO;

        for (OrderCommand.OrderItemCommand itemCommand : command.items()) {
            ProductSku sku = productSkuService.getBySkuIdWithLock(itemCommand.productSkuId());
            productSkuService.reserveStock(sku, itemCommand.quantity());
            initialPrice = initialPrice.add(BigDecimal.valueOf(sku.getPrice()).multiply(BigDecimal.valueOf(itemCommand.quantity())));
            order.addOrderItem(OrderItem.create(itemCommand.productSkuId(), itemCommand.quantity()));
        }
        order.updatePrice(initialPrice.longValue());

        // 쿠폰 적용
        couponService.applyCouponsToOrder(command, user, order);
        // 주문 저장
        Order savedOrder = orderService.saveOrder(order);
        // 상품 상태 업데이트
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
        // 주문 조회
        Order order = orderService.getOrder(command.orderId());
        // 취소 가능 여부 검증
        orderService.validateCancelable(order,user);
        // 재고 복구
        order.getOrderItems().forEach(item ->
                productSkuService.rollbackReservedStock(item.getProductSkuId(), item.getQuantity())
        );
        // 주문 상태 변경 및 저장
        orderService.cancelOrder(order);
        return OrderInfo.CancelOrder.from(order);
    }
}
