package com.loopers.interfaces.api.controller.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "사용자", description = "사용자관련API")
public interface UserV1ApiSpec {

    @Operation(
            summary = "회원가입",
            description = "로그인 ID, 성별, 생년월일, 이메일 정보를 기반으로 회원가입을 진행한다. "
                    + "성공 시 생성된 사용자 정보를 반환하며, 유효성 검증 실패 시 예외를 발생시킨다."
    )
    ApiResponse<UserV1Response> signUp(
            @Schema(name = "입력한 회원정보", description = "입력한 회원정보")
            UserV1Request.SignUp request
    );

    @Operation(
            summary = "내 정보 조회",
            description = "요청 헤더의 사용자 ID를 기반으로 현재 로그인한 사용자의 정보를 조회한다."
    )
    ApiResponse<UserV1Response> myProfile(
            @Schema(name = "예시 ID", description = "조회할 예시의 ID")
            String userId
    );
}
