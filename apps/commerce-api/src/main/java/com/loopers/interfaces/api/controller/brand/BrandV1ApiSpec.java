package com.loopers.interfaces.api.controller.brand;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "브랜드", description = "브랜드관련API")
public interface BrandV1ApiSpec {

    @Operation(
            summary = "브랜드정보 조회",
            description = "유효한 `브랜드ID`를 입력하면 `브랜드ID`, `브랜드명`, `설명` 등을 확인할 수 있다."
    )
    ApiResponse<BrandV1Response> getBrandInfo(
            @Schema(name = "브랜드ID", description = "사용자가 조회요청한 브랜드의 ID")
            @PathVariable Long brandId);
}
