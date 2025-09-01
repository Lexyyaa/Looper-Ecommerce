package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderInfo;

import java.util.List;

public interface OrderUsecase {
    OrderInfo.CreateOrder order(OrderCommand.CreateOrder command);
    List<OrderInfo.OrderListItem> getOrdersByUserId(String loginId);
    OrderInfo.OrderDetail getOrderDetail(Long orderId);
}
