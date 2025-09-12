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
        Map<Long, Long> viewMap = new HashMap<>();
        List<String> handledIds = new ArrayList<>();

        for (ConsumerRecord<Object, Object> r : records) {
            String eventId = io.header(r, "event_id");

            if (!handledService.shouldProcess("activity-log", eventId))
                continue;

            JsonNode body = objectMapper.readTree(io.payloadString(r)).path("payload");
            long productId = body.path("productId").asLong();

            viewMap.merge(productId, 1L, Long::sum);
            handledIds.add(eventId);
        }

        if (viewMap.isEmpty())
            return;

        metricsService.incProductViews(viewMap);

        Set<Long> productIds = new LinkedHashSet<>(viewMap.keySet());
        rankingService.computeAndPutScoresToday(productIds);

        handledService.saveAll("catalog-view", handledIds);
    }
}
