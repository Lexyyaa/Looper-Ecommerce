package com.loopers.interfaces.consumer.catalog;

import com.loopers.application.catalog.CatalogApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogProductBatchConsumer {

    private final CatalogApplicationService catalogApplicationService;

    @KafkaListener(
            topics = "${kafka.topic.catalog-product-events}",
            groupId = "catalog-product",
            containerFactory = "BATCH_LISTENER_WITH_DLQ",
            properties = {"auto.offset.reset=latest"}
    )
    public void onBatch(List<ConsumerRecord<Object, Object>> records, Acknowledgment ack) throws Exception {
        if (records == null || records.isEmpty()) {
            ack.acknowledge();
            return;
        }
        catalogApplicationService.onProductBatch(records);
        ack.acknowledge();
    }
}
