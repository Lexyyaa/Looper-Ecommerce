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
        PENDING, PAID, CANCELLED
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

    public void pay(Long userId) {
        if (this.status == Status.PAID) {
            throw new CoreException(ErrorType.CONFLICT, "이미 결제 완료 상태입니다.");
        }
        if (this.status == Status.CANCELLED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "취소된 건은 결제할 수 없습니다.");
        }
        this.status = Status.PAID;
    }

    public void cancel(Long userId) {
        if (this.getStatus() == Payment.Status.CANCELLED) {
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 취소된 결제입니다.");
        }
        if (this.status != Status.PAID) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 완료 건만 취소할 수 있습니다.");
        }
        this.status = Status.CANCELLED;
    }

    public static Payment create(Long userId, Long orderId, Long amount, Method method) {
        return Payment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .method(method)
                .status(Status.PENDING)
                .build();
    }
}
