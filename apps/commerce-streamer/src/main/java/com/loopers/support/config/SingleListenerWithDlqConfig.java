package com.loopers.support.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class SingleListenerWithDlqConfig {

    @Bean(name = "SINGLE_LISTENER_WITH_DLQ")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> singleListenerWithDlq(
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${kafka.topic.consumer-dlq}") String consumerDlqTopic
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(consumerDlqTopic, record.partition())
        );

        ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(5);
        backoff.setInitialInterval(200L);
        backoff.setMultiplier(2.0);
        backoff.setMaxInterval(5000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backoff);

        ConcurrentKafkaListenerContainerFactory<Object, Object> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(consumerFactory);
        f.setBatchListener(false);
        f.setConcurrency(2);
        f.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        f.setCommonErrorHandler(errorHandler);

        return f;
    }
    // DLQ 동작: 리스너에서 예외를 throw → 재시도(백오프) → 계속 실패 시 consumer-dlq로 원본 메시지 발행 → 오프셋 전진 → 다음 메시지 처리
}
