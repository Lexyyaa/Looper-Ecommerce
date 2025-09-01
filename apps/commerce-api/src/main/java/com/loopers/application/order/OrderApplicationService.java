package com.loopers.application.order;

import com.loopers.domain.coupon.*;
import com.loopers.domain.order.*;
import com.loopers.domain.product.ProductSku;
import com.loopers.domain.product.ProductSkuService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.shared.logging.Envelope;
import com.loopers.domain.monitoring.activity.ActivityPublisher;
import com.loopers.domain.monitoring.activity.payload.OrderActivityPayload;
import com.loopers.domain.monitoring.resultlog.ResultLogPublisher;
import com.loopers.domain.monitoring.resultlog.payload.OrderResultLogs;
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
    private final OrderService orderService;
    private final CouponService couponService;
    private final OrderEventPublisher orderEventPublisher;
    private final ActivityPublisher activityPublisher;
    private final ResultLogPublisher resultLogPublisher;

    @Override
    @Transactional
    public OrderInfo.CreateOrder order(OrderCommand.CreateOrder command) {
        // 사용자 활동로그(주문요청)
        activityPublisher.publish(Envelope.of(command.loginId(), new OrderActivityPayload.OrderRequested(command.getProductSkuIds())));

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
            // 주문목록에 추가
            order.addOrderItem(OrderItem.create(itemCommand.productSkuId(), itemCommand.quantity()));
        }
        // 주문서에 총 주문금액 추가
        order.updatePrice(initialPrice.longValue());

        UserCoupon userCoupon = null;
        if(command.hasCoupon()){
            userCoupon = couponService.applyCartCouponsToOrder(command, user, order);
        }
        // 주문 저장
        Order savedOrder = orderService.saveOrder(order);

        // 쿠폰사용처리 이벤트 발행
        orderEventPublisher.useCoupon(new OrderEvent.CouponUsed(order,userCoupon));
        // 재고 재계산 이벤트 발행
        orderEventPublisher.reCalStock(new OrderEvent.ReCalStock(order));
        // 주문 성공로그 이벤트 발행
        resultLogPublisher.publish(Envelope.of(command.loginId(), new OrderResultLogs.OrderSucceeded(savedOrder.getId())));

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
}
