package com.loopers.application.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PointPaymentProcessor implements PaymentProcessor {

    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Override
    public Payment.Method getMethod() {
        return Payment.Method.POINT;
    }

    @Override
    @Transactional // 포인트결제는 리트라이는 하지않아도된다고판단ㄱㄱ
    public Payment pay(User user, Order order, PaymentCommand.CreatePayment command) {
        Payment saved = null;

        //결제 생성
        Payment payment = paymentService.createPending(
                user.getId(),
                order.getId(),
                order.getFinalPrice(),
                command.method()
        );

        try{
            // 포인트 사용
            userService.usePoint(user, order.getFinalPrice());
            // 주문상태변경(주문완료)
            orderService.confirmOrder(order);
            // 결제상태변경(결제완료)
            saved = paymentService.confirmPayment(payment);
        }catch (CoreException e){
            // 결제상태변경(결제실패)
            saved = paymentService.cancelPayment(payment,e.getCustomMessage());
        }

        return saved;
    }
}
/**
 중요!: PointPaymentProcessor에서 결제가 실패하면 RuntimeException을 던져야 합니다.
 이렇게 하면 PaymentApplicationService의 메인 트랜잭션이 롤백되어,
 함께 시도된 카드 결제 건(상태: PENDING)도 DB에 저장되지 않게 됩니다.
 즉, 포인트가 부족하면 카드 결제 시도 자체를 막을 수 있습니다.
 */

//@Embeddable
//public record OrderNumber(String number) {
//
//    private static final String PREFIX = "29CART-";
//
//    public static OrderNumber initialize() {
//        String uuid = UUID.randomUUID()
//                .toString()
//                .replace("-", "")
//                .substring(0, 12)
//                .toUpperCase();
//        return new OrderNumber(PREFIX + uuid);
//    }
//}
//
//@Entity
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class User extends BaseEntity {
//
//    @NaturalId
//    @AttributeOverride(
//            name = "value",
//            column = @Column(
//                    name = "account_id",
//                    nullable = false,
//                    unique = true
//            )
//    )
//    private AccountId accountId;
