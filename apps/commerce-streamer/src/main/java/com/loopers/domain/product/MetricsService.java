package com.loopers.domain.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.infrastructure.product.ProductSkuMetricsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final ProductMetricsRepository productMetricsRepository;
    private final ProductSkuMetricsRepository productSkuMetricsRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void incProductViews(Map<Long, Long> viewMap, LocalDate date) {
        if (viewMap == null || viewMap.isEmpty())
            return;

        Instant now = Instant.now();
        for (Map.Entry<Long, Long> e : viewMap.entrySet()) {
            long productId = e.getKey();
            long delta = e.getValue() == null ? 0L : e.getValue();
            if (delta <= 0)
                continue;

            Optional<ProductMetrics> opt = productMetricsRepository.findByPkForUpdateWithLock(productId, date);
            if (opt.isPresent()) {
                ProductMetrics row = opt.get();
                row.addViewDelta(delta);
                row.setUpdatedAt(now);
                productMetricsRepository.save(row);
            } else {
                ProductMetrics fresh = ProductMetrics.newDailyRow(productId, date);
                fresh.addViewDelta(delta);
                fresh.setUpdatedAt(now);
                productMetricsRepository.save(fresh);
            }
        }
    }

    @Transactional
    public void onLikeChanged(String envelopeJson, String eventType, LocalDate date) throws Exception {
        JsonNode body = objectMapper.readTree(envelopeJson).path("payload");
        long productId = body.path("productId").asLong();

        long delta = 0L;
        if ("LikeAdded".equalsIgnoreCase(eventType)) {
            delta = 1L;
        } else if ("LikeRemoved".equalsIgnoreCase(eventType)) {
            delta = -1L;
        }

        Instant now = Instant.now();
        Optional<ProductMetrics> opt = productMetricsRepository.findByPkForUpdateWithLock(productId, date);
        if (opt.isPresent()) {
            var row = opt.get();
            row.addLikeDelta(delta);
            row.setUpdatedAt(now);
            productMetricsRepository.save(row);
        } else {
            var fresh = ProductMetrics.newDailyRow(productId, date);
            fresh.addLikeDelta(delta);
            fresh.setUpdatedAt(now);
            productMetricsRepository.save(fresh);
        }
    }

    @Transactional
    public void onStockConfirmed(String envelopeJson,LocalDate date) throws Exception {
        JsonNode p = objectMapper.readTree(envelopeJson).path("payload");
        Long productId = p.path("productId").asLong();
        Long productSkuId = p.path("productSkuId").asLong();
        long qty = p.path("amount").asLong();

        Instant now = Instant.now();
        Optional<ProductSkuMetrics> opt = productSkuMetricsRepository.findByPkForUpdateWithLock(productSkuId, date);
        if (opt.isPresent()) {
            var row = opt.get();
            row.addSalesDelta(qty);
            row.setUpdatedAt(now);
            productSkuMetricsRepository.save(row);
        } else {
            var fresh = ProductSkuMetrics.newDailyRow(productId, productSkuId, date);
            fresh.addSalesDelta(qty);
            fresh.setUpdatedAt(now);
            productSkuMetricsRepository.save(fresh);
        }
    }

    @Transactional(readOnly = true)
    public Map<Long, ProductMetrics> getProductMetrics(Set<Long> productIds, LocalDate date) {
        Map<Long, ProductMetrics> map = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return map;
        productMetricsRepository.findAllByIdProductIdInAndIdDate(productIds, date)
                .forEach(pm -> map.put(pm.getId().getProductId(), pm));
        return map;
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getSalesSumByProductIds(Set<Long> productIds, LocalDate date) {
        Map<Long, Long> sales = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return sales;
        List<ProductSkuMetricsJpaRepository.SalesSum> sums = productSkuMetricsRepository.sumSalesByProductIdsAndDate(productIds, date);
        sums.forEach(
                s -> sales.put(s.getProductId(), s.getTotal() == null ? 0L : s.getTotal())
        );
        return sales;
    }
}
