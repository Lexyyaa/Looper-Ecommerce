package com.loopers.application.eventlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.RecordIO;
import com.loopers.domain.eventhandle.EventHandledService;
import com.loopers.domain.eventlog.EventAuditService;
import com.loopers.domain.eventlog.EventLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditAppllicationService {

    private final RecordIO io;
    private final EventHandledService handledService;
    private final EventAuditService eventAuditService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void process(List<ConsumerRecord<Object, Object>> records) throws Exception {

        List<EventLog> toSave = new ArrayList<>();
        List<String> handledIds = new ArrayList<>();

        for (ConsumerRecord<Object, Object> r : records) {
            String eventId = io.header(r, "event_id");

            if (!handledService.shouldProcess("activity-log", eventId))
                continue;

            EventLog log = EventLog.create(
                    eventId,
                    io.header(r, "event_type"),
                    io.header(r, "event_version"),
                    io.header(r, "occurred_at"),
                    io.header(r, "actor_id"),
                    io.payloadString(r),
                    objectMapper.writeValueAsString(r.headers().toArray())
            );

            toSave.add(log);
            handledIds.add(eventId);
        }

        if (!toSave.isEmpty()) {
            eventAuditService.saveAll(toSave);
            handledService.saveAll("activity-log", handledIds);
        }
    }
}
