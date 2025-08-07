package com.loopers.interfaces.api.controller.payment;

import com.loopers.application.payment.PaymentUsecase;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders/{orderId}/payments")
public class PaymentController implements PaymentV1ApiSpec {

    private final PaymentUsecase paymentUsecase;

    @PostMapping
    public ApiResponse<PaymentV1Response.CreatePayment> createPayment(
            @PathVariable Long orderId,
            @RequestBody PaymentV1Request.CreatePayment request
    ) {
        PaymentCommand.CreatePayment command = new PaymentCommand.CreatePayment(
                request.loginId(),
                orderId,
                request.amount(),
                request.method()
        );

        var paymentInfo = paymentUsecase.createPayment(command);
        return ApiResponse.success(PaymentV1Response.CreatePayment.from(paymentInfo));
    }

    @DeleteMapping("/{paymentId}")
    public ApiResponse<PaymentV1Response.CancelPayment> cancelPayment(
            @PathVariable Long orderId,
            @PathVariable Long paymentId,
            @RequestBody PaymentV1Request.CancelPayment request
    ) {
        PaymentCommand.CancelPayment command = new PaymentCommand.CancelPayment(
                request.loginId(),
                orderId,
                paymentId
        );
        var cancelInfo = paymentUsecase.cancelPayment(command);
        return ApiResponse.success(PaymentV1Response.CancelPayment.from(cancelInfo));
    }
}
