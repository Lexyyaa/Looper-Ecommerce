package com.loopers.domain.like;

/** 활동(행위) 이벤트 — 좋아요/취소 */
public class LikeActivityPayload {
    public record LikeAdded(
            Long userId,
            String loginId,
            Long targetId,
            LikeTargetType targetType
    ) {}
    public record LikeRemoved(
            Long userId,
            String loginId,
            Long targetId,
            LikeTargetType targetType
    ) {}
}
