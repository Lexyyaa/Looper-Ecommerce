package com.loopers.interfaces.api.controller.like;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "좋아요", description = "좋아요 관련 API")
public interface LikeV1ApiSpec {

    @Operation(
            summary = "좋아요 등록",
            description = "사용자는 상품에 좋아요를 등록할 수 있다."
    )
    ApiResponse<LikeV1Response.Like> likeProduct(
            @Schema(name = "사용자정보", description = "사용자정보")
            @RequestHeader("X-USER-ID") String loginId,
            @Schema(name = "상품정보", description = "상품ID")
            @PathVariable Long productId
    );

    @Operation(
            summary = "좋아요 취소",
            description = "사용자는 상품에 좋아요를 취소할 수 있다."
    )
    @DeleteMapping("/products/{productId}")
    ApiResponse<?>  unlikeProduct(
            @RequestHeader("X-USER-ID") String loginId,
            @PathVariable Long productId
    );

    @Operation(
            summary = "내가 좋아요한 상품 목록 조회",
            description = "사용자는 자신이 좋아요한 상품 목록을 조회할 수 있다.."
    )
    @GetMapping("/{userId}/likes/products")
    ApiResponse<LikeV1Response.LikedProductList>  getLikedProducts(
            @Schema(name = "로그인 사용자 ID", description = "요청을 보낸 사용자의 고유 ID")
            @RequestHeader("X-USER-ID") String loginId,
            @Schema(name = "페이지 번호", description = "조회할 페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Schema(name = "페이지 당 항목 수", description = "한 페이지에 보여줄 상품의 개수")
            @RequestParam(defaultValue = "5") int size
    );
}
