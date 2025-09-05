package com.loopers.interfaces.consumer.product;

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
public class ProductMetricsConsumer {

    private final EventHandledService handled;
    private final MetricsService metrics;
    private final RecordIO io;

    @KafkaListener(
            topics = "${kafka.topic.catalog-events}",
            groupId = "catalog-metrics",
            containerFactory = "SINGLE_LISTENER_WITH_DLQ",
            properties = { "auto.offset.reset=latest" }
    )
    public void onMessage(ConsumerRecord<Object, Object> r, Acknowledgment ack) throws Exception {
        String eventId = io.header(r, "event_id");
        String type    = io.header(r, "event_type");
        String json    = io.payloadString(r);

        try {
            if (!handled.isExistEvent("catalog-metrics", eventId)) {
                ack.acknowledge(); return;
            }

            if ("LikeChanged".equals(type)) {
                metrics.onLikeChanged(json);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("ProductMetricsConsumer failed id={}", eventId, e);
            throw e;
        }
    }
}
