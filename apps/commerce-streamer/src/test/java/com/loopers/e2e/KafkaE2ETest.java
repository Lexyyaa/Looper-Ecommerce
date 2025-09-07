package com.loopers.e2e;


import com.loopers.domain.product.ProductMetrics;
import com.loopers.domain.product.ProductSkuMetrics;
import com.loopers.infrastructure.eventlog.EventLogJpaRepository;
import com.loopers.infrastructure.product.ProductMetricsJpaRepository;
import com.loopers.infrastructure.product.ProductSkuMetricsJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(
        classes = E2EAllLauncher.class,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "server.port=0",
                "management.server.port=0",
                "pg.base-url=localhost:18081",

                "kafka.topic.activity-events=activity-events",
                "kafka.topic.catalog-events=catalog-events",
                "kafka.topic.sku-events=sku-events",
                "kafka.topic.consumer-dlq=consumer-dlq",
                "kafka.topic.producer-dlq=producer-dlq"
        }
)
@EmbeddedKafka(
        topics = {"activity-events","catalog-events","sku-events","consumer-dlq","producer-dlq"},
        partitions = 1
)
@ActiveProfiles("test")
@DisplayName("KafkaE2ETest")
public class KafkaE2ETest {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired ProductMetricsJpaRepository productMetricsRepo;
    @Autowired ProductSkuMetricsJpaRepository productSkuMetricsRepo;
    @Autowired EventLogJpaRepository eventLogRepo;

    @Value("${kafka.topic.activity-events}")
    String activityTopic;
    @Value("${kafka.topic.catalog-events}")
    String catalogTopic;
    @Value("${kafka.topic.sku-events}")
    String skuTopic;

    @AfterEach
    void clean() {
        eventLogRepo.deleteAll();
        productMetricsRepo.deleteAll();
        productSkuMetricsRepo.deleteAll();
    }

    private ProducerRecord<String, Object> record(
            String topic, String key, String eventId, String type, String actorId, Map<String, Object> payload
    ) {
        Map<String, Object> envelope = Map.of(
                "id", eventId,
                "occurredAt", Instant.now().toString(),
                "actorId", actorId,
                "payload", payload
        );
        ProducerRecord<String, Object> r = new ProducerRecord<>(topic, key, envelope);
        r.headers()
                .add(new RecordHeader("event_id", eventId.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("event_type", type.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("event_version", "1".getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("occurred_at", Instant.now().toString().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("actor_id", actorId.getBytes(StandardCharsets.UTF_8)));
        return r;
    }

    @Test
    @DisplayName("활동 이벤트(ProductDetailViewed) 발행 → 스트리머가 소비하여 event_log에 기록")
    void activity_event_audit_log() {
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of("loginId", "user-301", "productId", 301);

        ProducerRecord<String, Object> rec =
                record(activityTopic, "user-301", eventId, "ProductDetailViewed", "user-301", payload);

        try {
            kafkaTemplate.send(rec).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Kafka send failed", e);
        }

        Awaitility.await().atMost(Duration.ofSeconds(8)).untilAsserted(() ->
                assertThat(eventLogRepo.findAll())
                        .anySatisfy(row -> assertThat(row.getEventId()).isEqualTo(eventId))
        );
    }

    @Test
    @DisplayName("카탈로그 이벤트(LikeChanged) 발행하고 product_metrics.likeCnt 갱신")
    void catalog_like_changed_metrics() {
        long productId = 101L;
        long likeCount = 7L;
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of("productId", productId, "targetType", "PRODUCT", "likeCount", likeCount);

        ProducerRecord<String, Object> rec =
                record(catalogTopic, "user-1", eventId, "LikeChanged", "user-1", payload);

        try {
            kafkaTemplate.send(rec).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Kafka send failed", e);
        }

        Awaitility.await().atMost(Duration.ofSeconds(8)).untilAsserted(() -> {
            Optional<ProductMetrics> found = productMetricsRepo.findById(productId);
            assertThat(found).isPresent();
            assertThat(found.get().getLikeCnt()).isEqualTo(likeCount);
        });
    }

    @Test
    @DisplayName("SKU 이벤트(StockConfirmed) 중복 발행 → salesCnt는 1회만 반영(멱등)")
    void sku_stock_confirmed_idempotent() {
        long skuId = 2001L;
        int amount = 3;
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of("productId", 201, "productSkuId", skuId, "amount", amount);

        ProducerRecord<String, Object> rec =
                record(skuTopic, "system", eventId, "StockConfirmed", "system", payload);

        try {
            kafkaTemplate.send(rec).get(5, TimeUnit.SECONDS);
            kafkaTemplate.send(rec).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Kafka send failed", e);
        }

        Awaitility.await().atMost(Duration.ofSeconds(8)).untilAsserted(() -> {
            Optional<ProductSkuMetrics> found = productSkuMetricsRepo.findById(skuId);
            assertThat(found).isPresent();
            assertThat(found.get().getSalesCnt()).isEqualTo(amount);
        });
    }
}
