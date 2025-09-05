package com.loopers.infrastructure.event.order;

import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.order.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisherImpl implements OrderEventPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public void useCoupon(OrderEvent.CouponUsed event) {
        publisher.publishEvent(event);
    }

    @Override
    public void reCalStock(OrderEvent.ReCalStock event) {
        publisher.publishEvent(event);
    }
}
