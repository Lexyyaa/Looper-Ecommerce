package com.loopers.interfaces.api.controller.point;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "포인트", description = "포인트관련API")
public interface PointV1ApiSpec {

    @Operation(
            summary = "포인트충전",
            description = "사용자의 포인트를 지정한 금액만큼 충전한다."
    )
    ApiResponse<PointV1Response> charge(
            @Schema(name = "사용자정보", description = "사용자정보")
            String loginId,
            @Schema(name = "충전금액", description = "충전금액")
            PointV1Request.Charge request
    );

    @Operation(
            summary = "포인트조회",
            description = "사용자의 현재 보유 포인트를 조회한다."
    )
    ApiResponse<PointV1Response> myPoint(
            @Schema(name = "예시 ID", description = "조회할 예시의 ID")
            @RequestHeader("X-User-Id") String userId
    );
}
