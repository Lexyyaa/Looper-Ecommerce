package com.loopers.interfaces.api.controller.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "상품", description = "상품 조회 관련 API")
public interface ProductV1ApiSpec {

    @Operation(
            summary = "상품 요약 목록 조회",
            description = "다양한 조건으로 상품 요약 정보를 목록 형태로 조회할 수 있습니다."
    )
    ApiResponse<ProductV1Response.Summaries> getProductSummaries(
            @Valid ProductV1Request.List request
    );

    @Operation(
            summary = "특정 상품 상세 조회",
            description = "특정 상품의 상세 정보를 조회합니다."
    )
    ApiResponse<ProductV1Response.Detail> getProductDetail(
            @Parameter(
                    name = "productId",
                    description = "조회할 상품의 고유 ID",
                    example = "1"
            )
            @PathVariable Long productId
    );
}
