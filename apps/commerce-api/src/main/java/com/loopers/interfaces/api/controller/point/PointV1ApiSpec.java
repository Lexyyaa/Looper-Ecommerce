package com.loopers.interfaces.api.controller.point;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "포인트", description = "포인트 충전 및 조회 관련 API")
public interface PointV1ApiSpec {

    @Operation(
            summary = "포인트 충전",
            description = "사용자의 포인트를 충전합니다."
    )
    ApiResponse<PointV1Response> charge(
            @Parameter(name = "X-USER-ID", description = "요청을 보낸 사용자의 고유 ID")
            @RequestHeader("X-USER-ID") String loginId,
            @RequestBody(
                    description = "충전에 필요한 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PointV1Request.Charge.class))
            ) PointV1Request.Charge request
    );

    @Operation(
            summary = "나의 현재 포인트 조회",
            description = "로그인한 사용자의 현재 보유 포인트를 조회합니다."
    )
    ApiResponse<PointV1Response> myPoint(
            @Parameter(name = "X-USER-ID", description = "요청을 보낸 사용자의 고유 ID")
            @RequestHeader("X-USER-ID") String loginId
    );
}
