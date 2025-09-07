package com.loopers.domain.like;

public interface LikeActivityPublisher {
    void like(LikeActivityPayload.LikeAdded event);
    void unlike(LikeActivityPayload.LikeRemoved event);
}
