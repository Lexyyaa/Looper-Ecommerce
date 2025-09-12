package com.loopers.application.rank;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.rank.Rank;
import com.loopers.domain.rank.RankCommand;
import com.loopers.domain.rank.RankInfo;
import com.loopers.domain.rank.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RankApplicationService implements RankUsecase {

    private final RankingService rankingService;
    private final ProductService productService;

    @Override
    @Transactional(readOnly = true)
    public List<RankInfo.ProductRank> getProductRanking(RankCommand.ProductRanking command) {
        LocalDate date = LocalDate.parse(command.date());
        int size = command.size();

        List<Rank> rankList = rankingService.getProductRank(date, size);

        if (rankList.isEmpty())
            return List.of();

        List<Long> ids = rankList.stream().map(Rank::getProductId).toList();

        Map<Long, Product> productMap = productService.getProducts(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return rankList.stream()
                .map(r -> RankInfo.ProductRank.from(r, productMap.get(r.getProductId())))
                .toList();
    }
}

