package com.loopers.interfaces.api.controller.payment;

import com.loopers.application.payment.PaymentUsecase;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController implements PaymentV1ApiSpec {

    private final PaymentUsecase paymentUsecase;

    @PostMapping("/{orderId}")
    public ApiResponse<PaymentV1Response.CreatePayment> createPayment(
            @PathVariable Long orderId,
            @RequestBody PaymentV1Request.CreatePayment request
    ) {
        PaymentCommand.CreatePayment command = PaymentCommand.CreatePayment.create(request, orderId);

        var paymentInfo = paymentUsecase.createPayment(command);
        return ApiResponse.success(PaymentV1Response.CreatePayment.from(paymentInfo));
    }
}
