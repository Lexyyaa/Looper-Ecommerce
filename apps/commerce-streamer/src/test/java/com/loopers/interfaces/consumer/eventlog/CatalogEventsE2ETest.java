package com.loopers.interfaces.consumer.eventlog;


import com.loopers.CommerceStreamerApplication;
import com.loopers.domain.product.ProductMetrics;
import com.loopers.infrastructure.product.ProductMetricsJpaRepository;
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
                "kafka.topic.catalog-events=catalog-events"
        }
)
@EmbeddedKafka(topics = {"catalog-events"}, partitions = 1)
@ActiveProfiles("test")
@DisplayName("CatalogEventsE2ETest")
class CatalogEventsE2ETest {

    @Autowired EmbeddedKafkaBroker broker;
    @Autowired ProductMetricsJpaRepository productMetricsJpaRepository;

    @AfterEach
    void clean() {
        log.info("[정리] product_metrics 테이블 데이터 삭제");
        productMetricsJpaRepository.deleteAll();
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
    @DisplayName("LikeChanged 이벤트 발행 시 product_metrics.likeCnt 가 지정값으로 upsert 된다")
    void likeChanged_updates_likeCnt() {
        long productId = 101L;
        long likeCount = 5L;
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of("productId", productId, "targetType", "PRODUCT", "likeCount", likeCount);

        log.info("productId={}, likeCount={}, eventId={}", productId, likeCount, eventId);

        try (KafkaProducer<String, Object> p = producer()) {
            p.send(record("catalog-events", eventId, "LikeChanged", "user-1", payload));
            p.flush();
        }

        Awaitility.await().atMost(Duration.ofSeconds(8)).untilAsserted(() -> {
            Optional<ProductMetrics> found = productMetricsJpaRepository.findById(productId);
            assertThat(found).isPresent();
            assertThat(found.get().getLikeCnt()).isEqualTo(likeCount);
        });
        log.info("product_metrics(productId={})의 likeCnt={} ", productId, likeCount);
    }

    @Test
    @DisplayName("중복 발행 멱등성: 같은 eventId의 LikeChanged를 2번 보내도 likeCnt는 1회만 반영된다")
    void likeChanged_is_idempotent_on_likeCnt() {
        long productId = 101L;
        long likeCount = 5L;
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of("productId", productId, "targetType", "PRODUCT", "likeCount", likeCount);

        try (KafkaProducer<String, Object> p = producer()) {
            ProducerRecord<String, Object> rec = record("catalog-events", eventId, "LikeChanged", "user-1", payload);
            p.send(rec);
            p.send(rec);
            p.flush();
        }

        Awaitility.await().atMost(Duration.ofSeconds(8)).untilAsserted(() -> {
            Optional<ProductMetrics> found = productMetricsJpaRepository.findById(productId);
            assertThat(found).isPresent();
            assertThat(found.get().getLikeCnt()).isEqualTo(likeCount);
        });
        log.info("likeCnt={} , productId={}", likeCount, productId);
    }

    @Test
    @DisplayName("중복 발행 멱등성: 같은 eventId의 ProductDetailViewed를 2번 보내도 viewCnt는 +1 한 번만 증가한다")
    void productDetailViewed_is_idempotent_on_viewCnt() {
        long productId = 301L;
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of("loginId", "user-301", "productId", productId);

        try (KafkaProducer<String, Object> p = producer()) {
            ProducerRecord<String, Object> rec = record("catalog-events", eventId, "ProductDetailViewed", "user-301", payload);
            p.send(rec);
            p.send(rec);
            p.flush();
        }

        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<ProductMetrics> found = productMetricsJpaRepository.findById(productId);
            assertThat(found).isPresent();
            assertThat(found.get().getViewCnt()).isEqualTo(1L);
        });
        log.info("ProductDetailViewed를 productId : {} ", productId);
    }
}
