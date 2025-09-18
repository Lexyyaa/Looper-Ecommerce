package com.loopers.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Component
public class RecordIO {
    private final ObjectMapper M = new ObjectMapper();

    private static final ZoneId zoneId = ZoneId.of("Asia/Seoul");

    public String header(ConsumerRecord<Object, Object> r, String key) {
        if (r.headers() == null || r.headers().lastHeader(key) == null) return "";
        return new String(r.headers().lastHeader(key).value(), StandardCharsets.UTF_8);
    }

    public Optional<String> headerOpt(ConsumerRecord<Object, Object> r, String key) {
        String v = header(r, key);
        return (v == null || v.isBlank()) ? Optional.empty() : Optional.of(v);
    }

    public LocalDate occurredDate(ConsumerRecord<Object, Object> r) {
        return headerDate(r, "occurred_at");
    }

    public LocalDate headerDate(ConsumerRecord<Object, Object> r, String key) {
        String occurredAt = header(r, key);
        try {
            if (occurredAt == null || occurredAt.isBlank()) return LocalDate.now(zoneId);
            Instant inst = Instant.parse(occurredAt);
            return inst.atZone(zoneId).toLocalDate();
        } catch (Exception ignore) {
            return LocalDate.now(zoneId);
        }
    }

    public JsonNode json(ConsumerRecord<Object, Object> r) throws Exception {
        byte[] bytes = (byte[]) r.value();
        return M.readTree(bytes);
    }

    public JsonNode payload(ConsumerRecord<Object, Object> r) throws Exception {
        return json(r).path("payload");
    }

    public String payloadString(ConsumerRecord<Object, Object> r) {
        return new String((byte[]) r.value(), StandardCharsets.UTF_8);
    }
}
