package com.loopers.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
public class RecordIO {
    private final ObjectMapper M = new ObjectMapper();

    public String header(ConsumerRecord<Object,Object> r, String key) {
        return r.headers().lastHeader(key) == null ? "" : new String(r.headers().lastHeader(key).value());
    }

    public JsonNode json(ConsumerRecord<Object,Object> r) throws Exception {
        byte[] bytes = (byte[]) r.value();
        return M.readTree(bytes);
    }

    public String payloadString(ConsumerRecord<Object,Object> r) {
        return new String((byte[]) r.value());
    }
}
