package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.user.User;

public interface  PaymentProcessor {

    Payment.Method getMethod();

    Payment pay(User user, Order order,PaymentCommand.CreatePayment command);
}
