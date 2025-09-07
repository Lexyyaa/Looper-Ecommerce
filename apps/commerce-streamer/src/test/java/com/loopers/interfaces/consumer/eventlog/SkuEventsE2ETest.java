package com.loopers.interfaces.consumer.eventlog;


import com.loopers.CommerceStreamerApplication;
import com.loopers.domain.product.ProductSkuMetrics;
import com.loopers.infrastructure.product.ProductSkuMetricsJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(
        classes = {CommerceStreamerApplication.class},
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "kafka.topic.sku-events=sku-events"
        }
)
@EmbeddedKafka(topics = {"sku-events"}, partitions = 1)
@ActiveProfiles("test")
@DisplayName("SkuEventsE2ETest")
class SkuEventsE2ETest {

    @Autowired EmbeddedKafkaBroker broker;
    @Autowired ProductSkuMetricsJpaRepository productSkuMetricsJpaRepository;

    @AfterEach
    void clean() {
        log.info("[정리] product_sku_metrics 테이블 데이터 삭제");
        productSkuMetricsJpaRepository.deleteAll();
    }

    private KafkaProducer<String, Object> producer() {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString(),
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                JsonSerializer.ADD_TYPE_INFO_HEADERS, false
        );
        return new KafkaProducer<>(props);
    }

    private ProducerRecord<String, Object> record(String topic, String eventId, String type, String actorId, Map<String, Object> payload) {
        Map<String, Object> envelope = Map.of(
                "id", eventId,
                "occurredAt", Instant.now().toString(),
                "actorId", actorId,
                "payload", payload
        );

        ProducerRecord<String, Object> r = new ProducerRecord<>(topic, actorId, envelope);
        r.headers()
                .add(new RecordHeader("event_id", eventId.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("event_type", type.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("event_version", "1".getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("occurred_at", Instant.now().toString().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("actor_id", actorId.getBytes(StandardCharsets.UTF_8)));
        return r;
    }

    @Test
    @DisplayName("중복 발행 멱등성: 같은 eventId의 StockConfirmed를 2번 보내도 salesCnt는 1회만 반영된다")
    void stockConfirmed_upserts_once_with_idempotency() {
        long skuId = 2001L;
        int amount = 3;
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of("productId", 201, "productSkuId", skuId, "amount", amount);

        log.info("[Given] skuId={}, amount={}, eventId={}", skuId, amount, eventId);

        try (KafkaProducer<String, Object> p = producer()) {
            ProducerRecord<String, Object> rec = record("sku-events", eventId, "StockConfirmed", "system", payload);
            p.send(rec);
            p.send(rec);
            p.flush();
        }

        log.info("컨슈머 처리 완료까지 대기 후 salesCnt 검증(멱등성)");
        Awaitility.await().atMost(Duration.ofSeconds(8)).untilAsserted(() -> {
            Optional<ProductSkuMetrics> found = productSkuMetricsJpaRepository.findById(skuId);
            assertThat(found).isPresent();
            assertThat(found.get().getSalesCnt()).isEqualTo(amount);
        });
        log.info("skuId={} salesCnt={} ", skuId, amount);
    }
}
