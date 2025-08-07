package com.loopers.domain.order;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,"존재하지 않는 주문입니다."));
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public void validateCancelable(Order order, User user) {
        if (!order.getUserId().equals(user.getId())) {
            throw new CoreException(ErrorType.BAD_REQUEST,"본인의 주문만 취소할 수 있습니다.");
        }
        if (order.getStatus() == Order.Status.CANCELED) {
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 취소된 주문입니다.");
        }
        if (order.getStatus() == Order.Status.CONFIRMED) {
            throw new CoreException(ErrorType.BAD_REQUEST,"결제 완료된 주문은 취소할 수 없습니다.");
        }
    }

    public void cancelOrder(Order order) {
        order.cancel();
        orderRepository.save(order);
    }
}
