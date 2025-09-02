package com.loopers.interfaces.api.controller.payment;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "결제", description = "결제 생성 및 취소 관련 API")
public interface PaymentV1ApiSpec {

    @Operation(
            summary = "결제 생성",
            description = "특정 주문에 대한 결제 정보를 생성합니다."
    )

    ApiResponse<PaymentV1Response.CreatePayment> createPayment(
            @Parameter(name = "orderId", description = "결제할 주문의 고유 ID")
            @PathVariable Long orderId,
            @RequestBody(
                    description = "결제에 필요한 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentV1Request.CreatePayment.class))
            ) PaymentV1Request.CreatePayment request
    );
}
