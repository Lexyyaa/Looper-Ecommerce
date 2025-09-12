package com.loopers.interfaces.api.controller.rank;

import com.loopers.application.rank.RankUsecase;
import com.loopers.domain.rank.RankCommand;
import com.loopers.domain.rank.RankInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankController implements RankV1ApiSpec{

    private final RankUsecase rankUsecase;

    @Override
    @GetMapping
    public ApiResponse<RankV1Response.ProductRankList> rank(
            @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<RankInfo.ProductRank> infos = rankUsecase.getProductRanking(RankCommand.ProductRanking.create(date,size));
        return ApiResponse.success(RankV1Response.ProductRankList.from(infos));
    }
}
