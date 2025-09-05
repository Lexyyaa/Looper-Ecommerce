package com.loopers.interfaces.consumer.eventlog;

import com.loopers.domain.RecordIO;
import com.loopers.domain.eventhandle.EventHandledService;
import com.loopers.domain.product.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityMetricsConsumer {

    private final EventHandledService eventHandledService;
    private final MetricsService metricsService;
    private final RecordIO io;

    @KafkaListener(
            topics = "${kafka.topic.activity-events}",
            groupId = "activity-metrics",
            containerFactory = "SINGLE_LISTENER_WITH_DLQ",
            properties = { "auto.offset.reset=latest" }
    )
    public void onMessage(ConsumerRecord<Object,Object> record, Acknowledgment ack) throws Exception {
        String eventId = io.header(record,"event_id");
        String type = io.header(record,"event_type");
        String json = io.payloadString(record);

        try {
            if (!eventHandledService.isExistEvent("activity-metrics", eventId)) {
                ack.acknowledge();
                return;
            }

            if ("ProductDetailViewed".equals(type)) {
                metricsService.onProductViewed(json);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("ActivityMetricsConsumer failed id : {}", eventId, e);
            throw e;
        }
    }
}
