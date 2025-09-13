package com.loopers.application.rank;

import com.loopers.domain.rank.RankCommand;
import com.loopers.domain.rank.RankInfo;

import java.util.List;

public interface RankUsecase {
    List<RankInfo.ProductRank> getProductRanking(RankCommand.ProductRanking command);
}
