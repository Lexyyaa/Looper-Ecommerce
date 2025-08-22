package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.paymentgateway.PaymentDetail;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_idempotency", columnNames = {"idempotencyKey"}),
        @UniqueConstraint(name = "uk_payment_txKey",    columnNames = {"txKey"})
})
public class Payment extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(nullable = false)
    private String txKey;

    @Column(nullable = false, length = 64)
    private String idempotencyKey;

    @Column(length = 255)
    private String failReason;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Method method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Method {
        POINT, CARD
    }

    public enum Status {
        PENDING, REQUESTED, SUCCESS, FAILED, CANCELED ,
    }

    public void toRequested(String txKey) {
        this.status = Status.REQUESTED;
        this.txKey = txKey;
    }

    public void toSuccess() {
        this.status = Status.SUCCESS;
        this.updateDate();
    }

    public void toFail(String reason) {
        this.status = Status.FAILED;
        this.failReason = reason;
        this.updateDate();
    }

    public void toCanceled() {
        this.status = Status.CANCELED;
        this.updateDate();
    }

    public void pay(Long userId) {
        if (this.status == Status.SUCCESS) {
            throw new CoreException(ErrorType.CONFLICT, "이미 결제 완료 상태입니다.");
        }
        if (this.status == Status.FAILED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "잘못된 접근입니다.");
        }
        if (this.status == Status.CANCELED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "취소된 건은 결제할 수 없습니다.");
        }
        this.status = Status.SUCCESS;
        this.updateDate();
    }

    public void cancel(Long userId) {
        if (this.getStatus() == Payment.Status.CANCELED) {
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 취소된 결제입니다.");
        }
        if (this.status != Status.SUCCESS) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 완료 건만 취소할 수 있습니다.");
        }
        this.status = Status.CANCELED;
        this.updateDate();
    }

    public static String generateIdemKey() {
        String uuid = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
        return uuid;
    }

    public static Payment create(Long userId, Long orderId, Long amount, Method method) {
        return Payment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .method(method)
                .status(Status.PENDING)
                .idempotencyKey(generateIdemKey())
                .build();
    }
}
