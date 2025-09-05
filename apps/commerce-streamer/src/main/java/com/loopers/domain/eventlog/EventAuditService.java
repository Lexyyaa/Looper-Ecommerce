package com.loopers.domain.eventlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.infrastructure.eventlog.EventLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventAuditService {

    private final EventLogJpaRepository eventLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void write(String eventId,
                      String eventType,
                      String version,
                      String occurredAt,
                      String actorId, String payloadJson, Headers headers) throws Exception {
        Map<String, String> hdr = new HashMap<>();

        headers.forEach(h -> hdr.put(h.key(), new String(h.value())));

        EventLog log = EventLog.builder()
                .eventId(eventId)
                .eventType(eventType)
                .eventVersion(version)
                .occurredAt(occurredAt == null || occurredAt.isBlank() ? Instant.now() : Instant.parse(occurredAt))
                .actorId(actorId)
                .payloadJson(payloadJson)
                .headersJson(objectMapper.writeValueAsString(hdr))
                .receivedAt(Instant.now())
                .build();
        eventLogRepository.save(log);
    }
}
