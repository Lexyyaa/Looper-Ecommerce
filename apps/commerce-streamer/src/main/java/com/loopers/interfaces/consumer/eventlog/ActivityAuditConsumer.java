package com.loopers.interfaces.consumer.eventlog;

import com.loopers.domain.RecordIO;
import com.loopers.domain.eventhandle.EventHandledService;
import com.loopers.domain.eventlog.EventAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityAuditConsumer {

    private final EventHandledService eventHandledService;
    private final EventAuditService eventAuditService;
    private final RecordIO io;

    @KafkaListener(
            topics = "${kafka.topic.activity-events}",
            groupId = "activity-audit",
            containerFactory = "SINGLE_LISTENER_WITH_DLQ",
            properties = { "auto.offset.reset=latest" }
    )
    public void onMessage(ConsumerRecord<Object,Object> record, Acknowledgment ack) throws Exception {
        String eventId = io.header(record,"event_id");

        try {
            if (!eventHandledService.isExistEvent("activity-audit", eventId)) {
                ack.acknowledge();
                return;
            }

            eventAuditService.write(
                    eventId,
                    io.header(record,"event_type"),
                    io.header(record,"event_version"),
                    io.header(record,"occurred_at"),
                    io.header(record,"actor_id"),
                    io.payloadString(record),
                    record.headers()
            );

            eventHandledService.save("activity-audit", eventId);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("ActivityAuditConsumer failed id : {}", eventId, e);
            throw e;
        }
    }
}
