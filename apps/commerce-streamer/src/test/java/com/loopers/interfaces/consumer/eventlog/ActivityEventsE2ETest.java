package com.loopers.interfaces.consumer.eventlog;

import com.loopers.CommerceStreamerApplication;
import com.loopers.infrastructure.eventlog.EventLogJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;



@Slf4j
@SpringBootTest(classes = {CommerceStreamerApplication.class})
@EmbeddedKafka(topics = {"activity-events"}, partitions = 1)
@ActiveProfiles("test")
@DisplayName("ActivityEventsE2ETest")
class ActivityEventsE2ETest {

    @Autowired
    EmbeddedKafkaBroker broker;

    @Autowired
    EventLogJpaRepository eventLogJpaRepository;

    @AfterEach
    void clean() {
        log.info("[정리] event_log 테이블 데이터 삭제");
        eventLogJpaRepository.deleteAll();
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
    @DisplayName("상품 상세 조회 활동 이벤트 발행하고 컨슈머가 수신하여 event_log에 기록한다")
    void productDetailViewed_writes_audit_log() {
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of("loginId", "user-301", "productId", 301);

        log.info("[Given] eventId={}, payload={}", eventId, payload);

        try (KafkaProducer<String, Object> p = producer()) {
            p.send(record("activity-events", eventId, "ProductDetailViewed", "user-301", payload));
            p.flush();
        }

        Awaitility.await().atMost(Duration.ofSeconds(8)).untilAsserted(() ->
                assertThat(eventLogJpaRepository.findAll())
                        .anySatisfy(logRow -> assertThat(logRow.getEventId()).isEqualTo(eventId))
        );
        log.info("eventId={}", eventId);
    }

    @Test
    @DisplayName("중복 발행 멱등성: 같은 eventId의 상품 상세 조회 이벤트를 2번 보내도 감사 로그는 1건만 저장된다")
    void productDetailViewed_is_idempotent_on_event_log() {
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of("loginId", "user-301", "productId", 301);

        try (KafkaProducer<String, Object> p = producer()) {
            ProducerRecord<String, Object> rec = record("activity-events", eventId, "ProductDetailViewed", "user-301", payload);
            p.send(rec);
            p.send(rec);
            p.flush();
        }

        Awaitility.await().atMost(Duration.ofSeconds(8)).untilAsserted(() -> {
            long sameId = eventLogJpaRepository.findAll().stream()
                    .filter(row -> eventId.equals(row.getEventId()))
                    .count();
            assertThat(sameId).isEqualTo(1L);
        });
        log.info("eventId={}", eventId);
    }
}
