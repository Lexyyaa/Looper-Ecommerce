package com.loopers.interfaces.api.controller.order;

import com.loopers.application.order.OrderUsecase;
import com.loopers.domain.order.OrderCommand;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController implements OrderV1ApiSpec {

    private final OrderUsecase orderUsecase;

    @PostMapping
    public ApiResponse<OrderV1Response.CreateOrder> createOrder(@RequestHeader("X-USER-ID") String loginId, @RequestBody OrderV1Request.CreateOrder request) {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder(
                loginId,
                request.items().stream()
                        .map(item -> new OrderCommand.OrderItemCommand(item.productSkuId(), item.quantity(), item.itemCouponId()))
                        .toList(),
                request.cartCouponId()
        );
        var orderInfo = orderUsecase.order(command);
        return ApiResponse.success(OrderV1Response.CreateOrder.from(orderInfo));
    }

    @GetMapping("")
    public ApiResponse<List<OrderV1Response.OrderListItem>> getOrders(@RequestHeader("X-USER-ID") String loginId) {
        var orders = orderUsecase.getOrdersByUserId(loginId)
                .stream()
                .map(OrderV1Response.OrderListItem::from)
                .toList();
        return ApiResponse.success(orders);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderV1Response.OrderDetail> getOrderDetail(@PathVariable Long orderId) {
        var orderDetail = orderUsecase.getOrderDetail(orderId);
        return ApiResponse.success(OrderV1Response.OrderDetail.from(orderDetail));
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderV1Response.CancelOrder> cancelOrder(
            @RequestHeader("X-USER-ID") String loginId,
            @PathVariable Long orderId
    ) {
        OrderCommand.CancelOrder command = new OrderCommand.CancelOrder(
                loginId,
                orderId
        );
        var cancelInfo = orderUsecase.cancelOrder(command);
        return ApiResponse.success(OrderV1Response.CancelOrder.from(cancelInfo));
    }
}
