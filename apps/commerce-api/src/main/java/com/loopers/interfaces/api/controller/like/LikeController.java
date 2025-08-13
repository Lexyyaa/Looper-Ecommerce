package com.loopers.interfaces.api.controller.like;

import com.loopers.application.like.LikeUsecase;
import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeInfo;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/likes")
public class LikeController implements LikeV1ApiSpec {

    private final LikeUsecase likeUsecase;

    @PostMapping("/products/{productId}")
    public ApiResponse<?> likeProduct(
            @RequestHeader("X-USER-ID") String loginId,
            @PathVariable Long productId
    ) {
        LikeCommand.Like command = LikeCommand.Like.of(loginId, productId, LikeTargetType.PRODUCT);
        likeUsecase.like(command);
        return ApiResponse.success();
    }

    @DeleteMapping("/products/{productId}")
    public ApiResponse<?>  unlikeProduct(
            @RequestHeader("X-USER-ID") String loginId,
            @PathVariable Long productId
    ) {
        LikeCommand.Like command = LikeCommand.Like.of(loginId, productId, LikeTargetType.PRODUCT);
        likeUsecase.unlike(command);
        return ApiResponse.success();
    }

    @GetMapping("/{userId}/products")
    public ApiResponse<LikeV1Response.LikedProductList>  getLikedProducts(
            @RequestHeader("X-USER-ID") String loginId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        LikeCommand.LikedProducts command = new LikeCommand.LikedProducts(loginId, LikeTargetType.PRODUCT, page, size);
        List<LikeInfo.LikedProduct> infos = likeUsecase.getLikedProducts(command);
        return ApiResponse.success(LikeV1Response.LikedProductList.from(infos));
    }
}
