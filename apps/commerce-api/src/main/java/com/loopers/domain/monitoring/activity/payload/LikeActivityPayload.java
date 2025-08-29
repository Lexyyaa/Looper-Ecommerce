package com.loopers.domain.monitoring.activity.payload;

import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.monitoring.activity.ActivityPayload;

public class LikeActivityPayload {
    public record LikeAdded(
            String loginId,
            Long targetId,
            LikeTargetType targetType
    )   implements ActivityPayload {
    }

    public record LikeRemoved(
            String loginId,
            Long targetId,
            LikeTargetType targetType
    ) implements ActivityPayload {
    }
}
