package com.loopers.support.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Outbox 없이도 전송 신뢰성 보강:
 * - 동기 send().get() + RetryTemplate
 * - 최종 실패 시 Producer DLQ 폴백
 */
@Configuration
public class ProducerRetryConfig {
    @Bean
    public RetryTemplate kafkaSendRetryTemplate() {
        RetryTemplate t = new RetryTemplate();
        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(5);
        t.setRetryPolicy(policy);

        ExponentialBackOffPolicy backoff = new ExponentialBackOffPolicy();
        backoff.setInitialInterval(200L);
        backoff.setMultiplier(2.0);
        backoff.setMaxInterval(5000L);
        t.setBackOffPolicy(backoff);
        return t;
    }
}
