package com.loopers.infrastructure.message;


import com.loopers.shared.event.Envelope;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;


@Component
@RequiredArgsConstructor
public class ProductSkuProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final RetryTemplate retryTemplate;

    @Value("${kafka.topic.catalog-product-events}")
    private String topic;

    @Value("${kafka.topic.producer-dlq}")
    private String dlq;

    public <T> void send(String key, Envelope<T> envelope) {
        String eventType = envelope.payload().getClass().getSimpleName();

        ProducerRecord<Object, Object> record = new ProducerRecord<>(topic, key, envelope);
        addHeaders(record, envelope.id(), eventType, envelope.at().toString(), envelope.actorId());

        try {
            retryTemplate.execute(ctx -> {
                kafkaTemplate.send(record).get();
                return null;
            });
        } catch (Exception e) {
            ProducerRecord<Object, Object> dead = new ProducerRecord<>(dlq, key, envelope);
            dead.headers().add(new RecordHeader("source_topic", topic.getBytes(StandardCharsets.UTF_8)));
            dead.headers().add(new RecordHeader("event_type",  eventType.getBytes(StandardCharsets.UTF_8)));
            kafkaTemplate.send(dead);
        }
    }

    private void addHeaders(ProducerRecord<Object, Object> record, String id, String type, String at, String actor) {
        record.headers()
                .add(new RecordHeader("event_id", id.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("event_type", type.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("event_version", "1".getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("occurred_at", at.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("actor_id", actor.getBytes(StandardCharsets.UTF_8)));
    }
}
