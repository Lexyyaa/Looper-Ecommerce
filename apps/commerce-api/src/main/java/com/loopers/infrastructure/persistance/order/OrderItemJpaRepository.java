package com.loopers.infrastructure.persistance.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
}
