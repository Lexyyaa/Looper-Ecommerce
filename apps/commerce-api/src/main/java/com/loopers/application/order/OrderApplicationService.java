package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductSku;
import com.loopers.domain.product.ProductSkuService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
@RequiredArgsConstructor
public class OrderApplicationService implements OrderUsecase {

    private final UserService userService;
    private final ProductSkuService productSkuService;
    private final ProductService productService;
    private final OrderService orderService;

    @Override
    @Transactional
    public OrderInfo.CreateOrder order(OrderCommand.CreateOrder command) {

        User user = userService.getUser(command.loginId());
        Order order = Order.create(user.getId(), 0L);

        // 주문항목 처리
        command.items().forEach(item -> {
            // 재고조회
            ProductSku sku = productSkuService.getBySkuId(item.productSkuId());
            // 재고선점
            productSkuService.reserveStock(sku, item.quantity());
            //관련상품의 모든 옵션애 대해 재고 판단.
            boolean isAllSoldOut = productSkuService.isAllSoldOut(sku.getProduct().getId());
            //isAllSoldOut == true 라면 상태값바꿈
            productService.updateStatus(isAllSoldOut,sku.getProduct().getId());
            // 가격합산
            order.addPrice((long) (sku.getPrice() * item.quantity()));
            // 주문항목 추가
            order.addOrderItem(OrderItem.create(sku.getId(), item.quantity()));
        });

        Order savedOrder = orderService.saveOrder(order);
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
