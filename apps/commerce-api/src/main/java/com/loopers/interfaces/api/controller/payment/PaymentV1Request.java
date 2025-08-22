package com.loopers.interfaces.api.controller.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.regex.Pattern;

public class PaymentV1Request {

    public record CreatePayment(
            String loginId,
            Long amount,
            Payment.Method method,
            String idempotencyKey,
            CardPaymentDetails details
    ) {}

    //TODO. 카드결제 DTO 추후 추상체와 구현체 분리
    public record CardPaymentDetails(
            String cardType,
            String cardNo
    ) {
        private static final Pattern REGEX_CARD_NO = Pattern.compile("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$");
        public CardPaymentDetails {
            if (cardNo == null || !REGEX_CARD_NO.matcher(cardNo).matches()) {
                throw new CoreException(ErrorType.BAD_REQUEST,"카드 번호는 xxxx-xxxx-xxxx-xxxx 형식이어야 합니다.");
            }
        }
    }

    public record CancelPayment(
            String loginId
    ) {}
}
