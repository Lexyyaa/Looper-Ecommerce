package com.loopers.application.monitoring;

import com.loopers.domain.monitoring.resultlog.ResultLogPayload;
import com.loopers.domain.monitoring.resultlog.ResultLogService;
import com.loopers.shared.logging.Envelope;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ResultLogListener {

    private final ResultLogService resultLogService;

    @Async("applicationEventTaskExecutor")
    @EventListener
    public void onSuccessOrNone(Envelope<? extends ResultLogPayload> env) {
        resultLogService.write(env);
    }

    @Async("applicationEventTaskExecutor")
    @EventListener
    public void onFail(Envelope<? extends ResultLogPayload> env) {
        resultLogService.write(env);
    }
}
