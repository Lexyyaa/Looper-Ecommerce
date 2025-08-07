package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELED
    }

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "price", nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        item.setOrder(this);
    }

    public void addPrice(Long amount) {
        this.price += amount;
    }

    public void cancel() {
        if (this.status == Status.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }
        this.status = Status.CANCELED;
    }

    public void isConfirmed() {
        if (this.status == Status.CONFIRMED) {
            throw new IllegalStateException("이미 결제된 주문입니다.");
        }
    }

    public void confirm() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("결제 전 상태에서만 확정할 수 있습니다.");
        }
        this.status = Status.CONFIRMED;
    }

    public static Order create(Long userId,Long price) {
        return Order.builder()
                .userId(userId)
                .price(price)
                .status(Status.PENDING)
                .build();
    }
}
