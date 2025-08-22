package com.loopers.infrastructure.pg;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="pgClient", url="${pg.base-url}", configuration= PgFeignConfig.class)
public interface PgFeignClient {

    @PostMapping("/api/v1/payments")
    PgFeignDto.PgPayResponse pay(@RequestHeader("X-USER-ID") String pgUserId, @RequestBody PgFeignDto.PgPayRequest body);

    @GetMapping("/api/v1/payments/{txKey}")
    PgFeignDto.PgPayDetail get(@RequestHeader("X-USER-ID") String pgUserId, @PathVariable String txKey);

    @GetMapping("/api/v1/payments")
    PgFeignDto.PgTxList byOrder(@RequestHeader("X-USER-ID") String pgUserId, @RequestParam String orderId);

}
