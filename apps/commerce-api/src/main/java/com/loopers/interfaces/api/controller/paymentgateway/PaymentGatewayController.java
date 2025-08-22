package com.loopers.interfaces.api.controller.paymentgateway;

import com.loopers.application.payment.PaymentUsecase;
import com.loopers.domain.payment.PaymentCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pg")
public class PaymentGatewayController implements PaymentGatewayV1ApiSpec {

    private final PaymentUsecase paymentUsecase;

    @PostMapping("/callback")
    public void getPgCallback(@RequestBody PaymentGatewayV1Request.TransactionInfo request) {
        PaymentCommand.SyncPayment command = PaymentCommand.SyncPayment.create(request);
        paymentUsecase.syncPayment(command);
    }
}
