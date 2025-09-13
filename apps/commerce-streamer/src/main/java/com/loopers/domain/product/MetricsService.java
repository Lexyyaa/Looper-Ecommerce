package com.loopers.domain.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.infrastructure.product.SalesSum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final ProductMetricsRepository productMetricsRepository;
    private final ProductSkuMetricsRepository productSkuMetricsRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void incProductViews(Map<Long, Long> viewMap) {
        for (Map.Entry<Long, Long> e : viewMap.entrySet()) {
            long productId = e.getKey();
            long viewCnt = e.getValue() == null ? 0L : e.getValue();
            if (viewCnt <= 0)
                continue;

            ProductMetrics productMetrics = productMetricsRepository.findById(productId)
                    .orElseGet(() -> ProductMetrics.builder()
                            .productId(productId)
                            .likeCnt(0)
                            .viewCnt(0)
                            .updatedAt(Instant.now())
                            .build());

            productMetrics.increaseViewCnt(viewCnt);

            productMetricsRepository.save(productMetrics);
        }
    }

    @Transactional
    public void onLikeChanged(String envelopeJson) throws Exception {
        JsonNode body = objectMapper.readTree(envelopeJson).path("payload");
        long productId = body.path("productId").asLong();
        long likeCnt   = body.path("likeCount").asLong();

        ProductMetrics productMetrics = productMetricsRepository.findById(productId)
                .orElseGet(() -> ProductMetrics.builder()
                        .productId(productId)
                        .likeCnt(0)
                        .viewCnt(0)
                        .updatedAt(Instant.now())
                        .build());

        productMetrics.setLikeCntLatest(likeCnt);

        productMetricsRepository.save(productMetrics);
    }

    @Transactional
    public void onStockConfirmed(String envelopeJson) throws Exception {
        JsonNode payload = objectMapper.readTree(envelopeJson).path("payload");

        Long productId = payload.path("productId").asLong();
        Long productSkuId = payload.path("productSkuId").asLong();
        Long qty = payload.path("amount").asLong();

        productSkuMetricsRepository.upsertAddSales(
                productId,
                productSkuId,
                qty,
                Instant.now()
        );
    }

    @Transactional(readOnly = true)
    public Map<Long, ProductMetrics> getProductMetrics(Set<Long> productIds) {
        Map<Long, ProductMetrics> pm = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return pm;

        for (Long id : productIds) {
            productMetricsRepository.findById(id).ifPresent(m -> pm.put(id, m));
        }
        return pm;
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getSalesSumByProductIds(Set<Long> productIds) {
        Map<Long, Long> sales = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return sales;

        List<SalesSum> sums = productSkuMetricsRepository.sumSalesByProductIds(productIds);
        for (SalesSum s : sums) {
            sales.put(s.getProductId(), s.getTotal() == null ? 0L : s.getTotal());
        }
        return sales;
    }
}
