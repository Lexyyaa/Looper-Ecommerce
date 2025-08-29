package com.loopers.domain.order;

public interface OrderEventPublisher {
    public void useCoupon(OrderEvent.CouponUsed event);
    public void reCalStock(OrderEvent.ReCalStock event);
}
