package com.loopers.application.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.RecordIO;
import com.loopers.domain.eventhandle.EventHandledService;
import com.loopers.domain.product.MetricsService;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CatalogApplicationService {

    private final RecordIO io;
    private final EventHandledService handledService;
    private final MetricsService metricsService;
    private final RankingService rankingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void onViewBatch(List<ConsumerRecord<Object, Object>> records) throws Exception {
        if (records == null || records.isEmpty())
            return;

        Map<LocalDate, Map<Long, Long>> viewByDate = new LinkedHashMap<>();
        List<String> handledIds = new ArrayList<>();

        for (ConsumerRecord<Object, Object> record : records) {
            String eventId = io.header(record, "event_id");
            if (!handledService.shouldProcess("catalog-view", eventId)) continue;

            long productId = io.payload(record).path("productId").asLong();
            if (productId <= 0) continue;

            LocalDate date = io.occurredDate(record);

            viewByDate.computeIfAbsent(date, d -> new HashMap<>())
                    .merge(productId, 1L, Long::sum);

            handledIds.add(eventId);
        }

        if (viewByDate.isEmpty())
            return;

        for (Map.Entry<LocalDate, Map<Long, Long>> entry : viewByDate.entrySet()) {
            LocalDate date = entry.getKey();
            Map<Long, Long> viewMap = entry.getValue();

            metricsService.incProductViews(viewMap, date);
            rankingService.computeAndPutScores(date, viewMap.keySet());
        }

        handledService.saveAll("catalog-view", handledIds);
    }

    public void onLikeBatch(List<ConsumerRecord<Object, Object>> records) throws Exception {
        if (records == null || records.isEmpty()) return;

        Map<LocalDate, Set<Long>> touchedByDate = new LinkedHashMap<>();
        List<String> handledIds = new ArrayList<>();

        for (ConsumerRecord<Object, Object> r : records) {
            String eventId = io.header(r, "event_id");
            if (!handledService.shouldProcess("catalog-like", eventId)) continue;

            String eventType = io.header(r, "event_type");
            LocalDate date = io.occurredDate(r);

            long productId = io.payload(r).path("targetId").asLong();
            if (productId <= 0) continue;

            JsonNode payload = objectMapper.createObjectNode().put("productId", productId);
            JsonNode envelope = objectMapper.createObjectNode().set("payload", payload);

            metricsService.onLikeChanged(envelope.toString(), eventType, date);

            touchedByDate.computeIfAbsent(date, d -> new LinkedHashSet<>()).add(productId);
            handledIds.add(eventId);
        }

        for (Map.Entry<LocalDate, Set<Long>> e : touchedByDate.entrySet()) {
            rankingService.computeAndPutScores(e.getKey(), e.getValue());
        }

        handledService.saveAll("catalog-like", handledIds);
    }

    public void onProductBatch(List<ConsumerRecord<Object, Object>> records) throws Exception {
        if (records == null || records.isEmpty()) return;

        Map<LocalDate, Set<Long>> touchedByDate = new LinkedHashMap<>();
        List<String> handledIds = new ArrayList<>();

        for (ConsumerRecord<Object, Object> r : records) {
            String eventId = io.header(r, "event_id");
            if (!handledService.shouldProcess("catalog-product", eventId)) continue;

            JsonNode body = io.payload(r);
            long productId = body.path("productId").asLong();
            long productSkuId = body.path("productSkuId").asLong();
            long amount = body.path("amount").asLong();
            if (productId <= 0 || productSkuId <= 0 || amount <= 0) continue;

            LocalDate date = io.occurredDate(r);

            JsonNode envelope = objectMapper.createObjectNode().set("payload", body);
            metricsService.onStockConfirmed(envelope.toString(), date);

            touchedByDate.computeIfAbsent(date, d -> new LinkedHashSet<>()).add(productId);
            handledIds.add(eventId);
        }

        for (Map.Entry<LocalDate, Set<Long>> e : touchedByDate.entrySet()) {
            rankingService.computeAndPutScores(e.getKey(), e.getValue());
        }

        handledService.saveAll("catalog-product", handledIds);
    }
}
