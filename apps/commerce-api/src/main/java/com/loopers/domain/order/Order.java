package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.coupon.CouponUsage;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "final_price", nullable = false)
    private Long finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELED
    }

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        item.setOrder(this);
    }

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CouponUsage> couponUsages = new ArrayList<>();

    public void addCouponUsage(CouponUsage usage) {
        if (!this.couponUsages.contains(usage)) {
            this.couponUsages.add(usage);
        }
        if (usage.getOrder() != this) {
            usage.setOrder(this);
        }
    }

    public void addPrice(Long amount) {
        if(amount < 1){
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 1원 이상이어야 합니다.");
        }
        this.price += amount;
    }

    public void updatePrice(Long amount) {
        if(amount < 1){
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 1원 이상이어야 합니다.");
        }
        this.price = amount;
    }

    public void updateFinalPrice(Long newFinalPrice) {
        this.finalPrice = newFinalPrice;
    }

    public void cancel() {
        if (this.status == Status.CANCELED) {
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 취소된 주문입니다.");
        }
        this.status = Status.CANCELED;
    }

    public void isConfirmed() {
        if (this.status == Status.CONFIRMED) {
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 결제된 주문입니다.");
        }
    }

    public void confirm() {
        if (this.status != Status.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST,"결제 전 상태에서만 확정할 수 있습니다.");
        }
        this.status = Status.CONFIRMED;
    }

    public static Order create(Long userId,Long price) {
        return Order.builder()
                .userId(userId)
                .price(price)
                .finalPrice(price)
                .status(Status.PENDING)
                .build();
    }
}
