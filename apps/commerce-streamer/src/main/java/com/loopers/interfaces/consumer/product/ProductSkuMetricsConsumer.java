package com.loopers.interfaces.consumer.product;

import com.loopers.domain.eventhandle.EventHandledService;
import com.loopers.domain.product.MetricsService;
import com.loopers.domain.RecordIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSkuMetricsConsumer {

    private final EventHandledService eventHandledService;
    private final MetricsService metricsService;
    private final RecordIO io;

    @KafkaListener(
            topics = "${kafka.topic.sku-events}",
            groupId = "order-metrics",
            containerFactory = "SINGLE_LISTENER_WITH_DLQ",
            properties = { "auto.offset.reset=latest" }
    )
    public void onMessage(ConsumerRecord<Object, Object> record, Acknowledgment ack) throws Exception {
        String eventId = io.header(record, "event_id");
        String type = io.header(record, "event_type");
        String json = io.payloadString(record);

        try {
            if (!eventHandledService.isExistEvent("product-sku-metrics", eventId)) {
                ack.acknowledge();
                return;
            }

            if ("StockConfirmed".equals(type)) {
                metricsService.onStockConfirmed(json);
            }

            eventHandledService.save("product-sku-metrics", eventId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("OrderMetricsConsumer failed id={} type={}", eventId, type, e);
            throw e;
        }
    }
}
