package com.loopers.domain.pg;

import com.loopers.domain.paymentgateway.PaymentDetail;
import com.loopers.domain.paymentgateway.PaymentGatewayRequest;
import com.loopers.domain.paymentgateway.PaymentGatewayResponse;

import java.util.Optional;

public interface PgClientAdapter {
    PaymentGatewayResponse acceptPayment(PaymentGatewayRequest req);
    PaymentDetail getByTxKey(String txKey);
    Optional<PaymentDetail> findSingleSuccessPayment(String orderId);
}
