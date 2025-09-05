package com.loopers.domain.like;

public class LikeEvent {

    public record Added(
            Long userId,
            String loginId,
            Long targetId,
            LikeTargetType targetType
    )  {}

    public record Removed(
            Long userId,
            String loginId,
            Long targetId,
            LikeTargetType targetType
    )  {}
}
