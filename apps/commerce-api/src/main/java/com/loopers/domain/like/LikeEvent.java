package com.loopers.domain.like;

import java.time.Instant;

public class LikeEvent {

    public record Added(
            Long userId,
            Long targetId,
            LikeTargetType targetType
    )  {}

    public record Removed(
            Long userId,
            Long targetId,
            LikeTargetType targetType
    )  {}
}
