package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentInfo;

public interface PaymentUsecase {
    PaymentInfo.CreatePayment createPayment(PaymentCommand.CreatePayment command);
}
