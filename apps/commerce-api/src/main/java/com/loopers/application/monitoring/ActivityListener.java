package com.loopers.application.monitoring;

import com.loopers.domain.monitoring.activity.ActivityPayload;
import com.loopers.domain.monitoring.activity.ActivityService;
import com.loopers.shared.logging.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityListener  {

    private final ActivityService activityService;

    @Async("applicationEventTaskExecutor")
    @EventListener
    public void onActivity(Envelope<? extends ActivityPayload> env) {
        activityService.write(env);
    }
}
