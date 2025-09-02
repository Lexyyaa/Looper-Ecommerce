package com.loopers.interfaces.api.controller.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "주문", description = "주문 생성, 조회, 취소 관련 API")
public interface OrderV1ApiSpec {

    @Operation(
            summary = "주문 생성",
            description = "사용자가 장바구니에 담은 상품으로 주문을 생성한다."
    )
    @PostMapping
    ApiResponse<OrderV1Response.CreateOrder> createOrder(
            @Parameter(name = "X-USER-ID", description = "요청을 보낸 사용자의 고유 ID")
            @RequestHeader("X-USER-ID") String loginId,
            @RequestBody OrderV1Request.CreateOrder request
    );

    @Operation(
            summary = "주문 목록 조회",
            description = "로그인한 사용자의 전체 주문 목록을 조회한다."
    )
    @GetMapping
    ApiResponse<List<OrderV1Response.OrderListItem>> getOrders(
            @Parameter(name = "X-USER-ID", description = "요청을 보낸 사용자의 고유 ID")
            @RequestHeader("X-USER-ID") String loginId
    );

    @Operation(
            summary = "특정 주문 상세 조회",
            description = "특정 주문에 대한 상세 정보를 조회한다."
    )
    @GetMapping("/{orderId}")
    ApiResponse<OrderV1Response.OrderDetail> getOrderDetail(
            @Parameter(name = "orderId", description = "조회할 주문의 고유 ID")
            @PathVariable Long orderId
    );
}
