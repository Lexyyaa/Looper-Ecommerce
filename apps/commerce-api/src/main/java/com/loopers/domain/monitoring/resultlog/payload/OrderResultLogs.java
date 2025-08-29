package com.loopers.domain.monitoring.resultlog.payload;

import com.loopers.domain.monitoring.resultlog.ResultLogPayload;

public class OrderResultLogs {
    public record OrderSucceeded(Long orderId) implements ResultLogPayload {}
}
