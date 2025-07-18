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
}
