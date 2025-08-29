package com.loopers.domain.monitoring.activity.payload;

import com.loopers.domain.monitoring.activity.ActivityPayload;

import java.util.List;

public class OrderActivityPayload {
    public record OrderRequested (
            List<Long> skuIds
    ) implements ActivityPayload {

    }
}
