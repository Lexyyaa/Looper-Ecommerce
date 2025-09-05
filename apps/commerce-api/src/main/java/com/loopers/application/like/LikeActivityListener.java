package com.loopers.application.like;

import com.loopers.domain.like.LikeActivityPayload;
import com.loopers.infrastructure.message.ActivityProducer;
import com.loopers.shared.event.Envelope;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeActivityListener {

    private final ActivityProducer activityProducer;

    @Async("applicationEventTaskExecutor")
    @EventListener
    public void onLikeAdded(Envelope<LikeActivityPayload.LikeAdded> event) {
        activityProducer.send(event);
    }

    @Async("applicationEventTaskExecutor")
    @EventListener
    public void onLikeRemoved(Envelope<LikeActivityPayload.LikeRemoved> event) {
        activityProducer.send(event);
    }
}
