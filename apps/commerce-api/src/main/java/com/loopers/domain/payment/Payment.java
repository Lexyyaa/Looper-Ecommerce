package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment")
public class Payment extends BaseEntity {

    public enum Method {
        POINT, CREDIT
    }

    public enum Status {
        PAID, CANCELLED
    }

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Method method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public void cancel() {
        if (this.status == Status.CANCELLED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 취소된 결제입니다.");
        }
        this.status = Status.CANCELLED;
    }

    public static Payment create(Long userId, Long orderId, Long amount, Method method) {
        return Payment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .method(method)
                .status(Status.PAID)
                .build();
    }
}
