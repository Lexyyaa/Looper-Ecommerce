package com.loopers.domain.like;

public interface LikeEventPublisher {
    void like(LikeEvent.Added event);
    void unlike(LikeEvent.Removed event);
}
