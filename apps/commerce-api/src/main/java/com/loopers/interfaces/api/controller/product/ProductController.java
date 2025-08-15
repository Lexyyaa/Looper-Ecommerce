package com.loopers.interfaces.api.controller.product;

import com.loopers.application.product.ProductUsecase;
import com.loopers.domain.product.ProductInfo;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductUsecase productUsecase;

    @GetMapping
    public ApiResponse<ProductV1Response.Summaries> getProductSummaries(
            @Valid ProductV1Request.List request
    ) {
        List<ProductInfo.Summary> infos = productUsecase.getProductSummaries(request.toCommand());
        return ApiResponse.success(ProductV1Response.Summaries.from(infos));
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductV1Response.Detail> getProductDetail(@PathVariable Long productId) {
//        ProductInfo.Detail detail = productUsecase.getProductDetail(productId);
        ProductInfo.Detail detail = productUsecase.getProductDetailWithCacheable(productId);
        return ApiResponse.success(ProductV1Response.Detail.from(detail));
    }
}

