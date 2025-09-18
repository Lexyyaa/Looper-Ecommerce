package com.loopers.interfaces.api.controller.rank;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "랭킹", description = "랭킹 관련 API")
public interface RankV1ApiSpec {
    @Operation(
            summary = "랭킹 조회",
            description = "사용자가 요청한 날짜, 사이즈에 대해 상품랭킹 조회"
    )
    ApiResponse<RankV1Response.ProductRankList> rank(
            @RequestParam String date,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "daily") String period
    );
}
