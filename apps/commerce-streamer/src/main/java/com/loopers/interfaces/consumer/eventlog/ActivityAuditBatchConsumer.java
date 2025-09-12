package com.loopers.interfaces.consumer.eventlog;

import com.loopers.application.eventlog.AuditAppllicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityAuditBatchConsumer {

    private final AuditAppllicationService auditAppllicationService;

    @KafkaListener(
            topics = "${kafka.topic.activity-events}",
            groupId = "activity-log",
            containerFactory = "BATCH_LISTENER_WITH_DLQ",
            properties = { "auto.offset.reset=latest" }
    )
    public void onBatch(List<ConsumerRecord<Object, Object>> records, Acknowledgment ack) throws Exception {
        if (records == null || records.isEmpty()) {
            ack.acknowledge();
            return;
        }

        auditAppllicationService.process(records);

        ack.acknowledge();
    }
}
