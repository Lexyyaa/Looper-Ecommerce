package com.loopers.domain.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final ProductMetricsRepository productMetricsRepository;
    private final ProductSkuMetricsRepository productSkuMetricsRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void onProductViewed(String envelopeJson) throws Exception {
        JsonNode root = objectMapper.readTree(envelopeJson);
        long productId = root.path("payload").path("productId").asLong();

        ProductMetrics productMetrics = productMetricsRepository.findById(productId)
                .orElseGet(() -> ProductMetrics.builder()
                        .productId(productId)
                        .likeCnt(0).viewCnt(0)
                        .updatedAt(Instant.now())
                        .build());

        productMetrics.incView(1);
        productMetricsRepository.save(productMetrics);
    }

    @Transactional
    public void onLikeChanged(String envelopeJson) throws Exception {
        JsonNode body = objectMapper.readTree(envelopeJson).path("payload");
        long productId = body.path("productId").asLong();
        long likeCnt   = body.path("likeCount").asLong();

        ProductMetrics m = productMetricsRepository.findById(productId)
                .orElseGet(() -> ProductMetrics.builder()
                        .productId(productId)
                        .likeCnt(0).viewCnt(0)
                        .updatedAt(Instant.now())
                        .build());

        m.setLikeCntLatest(likeCnt);
        productMetricsRepository.save(m);
    }

    @Transactional
    public void onStockConfirmed(String envelopeJson) throws Exception {
        JsonNode root = objectMapper.readTree(envelopeJson);
        JsonNode payload = root.path("payload");

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
}
