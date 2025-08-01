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
@Transactional
public class OrderApplicationService implements OrderUsecase {

    private final UserService userService;
    private final ProductSkuService productSkuService;
    private final ProductService productService;
    private final OrderService orderService;

    @Override
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
    public List<OrderInfo.OrderListItem> getOrdersByUserId(String loginId) {
        // 사용자조회
        User user = userService.getUser(loginId);

        return orderService.getOrdersByUserId(user.getId())
                .stream()
                .map(OrderInfo.OrderListItem::from)
                .toList();
    }

    @Override
    public OrderInfo.OrderDetail getOrderDetail(Long orderId) {
        Order order = orderService.getOrder(orderId);
        return OrderInfo.OrderDetail.from(order);
    }
}
