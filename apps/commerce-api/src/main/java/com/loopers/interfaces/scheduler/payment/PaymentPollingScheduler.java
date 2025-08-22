package com.loopers.interfaces.scheduler.payment;

import com.loopers.application.payment.PaymentUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentPollingScheduler {

    private static final long FIXED_DELAY_MS = 180_000L;

    private final PaymentUsecase paymentUsecase;

    @Scheduled(fixedDelay = FIXED_DELAY_MS)
    public void run() {
        try {
            paymentUsecase.pollRecentPayments();
        } catch (Exception e) {
            log.error("Payment poll batch error", e.getMessage());
        }
    }
}
