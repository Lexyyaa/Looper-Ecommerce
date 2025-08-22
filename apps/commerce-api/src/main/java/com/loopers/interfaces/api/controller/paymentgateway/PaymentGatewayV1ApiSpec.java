package com.loopers.interfaces.api.controller.paymentgateway;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "PG연동", description = "PG연동 관련 API")
public interface PaymentGatewayV1ApiSpec {

    @Operation(
            summary = "PG Callback",
            description = "결제요청 후 처리가 완료되면 호출하는 콜백 API"
    )
    void getPgCallback(@RequestBody PaymentGatewayV1Request.TransactionInfo transactionInfo) ;
}
