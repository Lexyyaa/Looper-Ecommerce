package com.loopers.infrastructure.pg;

import feign.Request;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Configuration
public class PgFeignConfig {

    @Bean
    Request.Options options() {
        return new Request.Options(200, TimeUnit.MILLISECONDS, 1000, TimeUnit.MILLISECONDS, true);
    }

    @Bean
    public ErrorDecoder pgErrorDecoder() {
        return (methodKey, response) -> {
            int status = response.status();

            // 짧은 오류 메시지용 바디 추출(최대 200자)
            String body = "";
            try {
                if (response.body() != null) {
                    String s = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    body = s.length() > 200 ? s.substring(0, 200) + "…" : s;
                }
            } catch (IOException ignored) {}

            // 408/429/5xx → 재시도 대상(RetryableException)로 표준화
            if (status == 408 || status == 429 || status >= 500) {
                // (옵션) Retry-After 헤더가 "초" 숫자면 힌트로 반영
                Date retryAfter = null;
                if (response.headers() != null) {
                    Collection<String> vals = response.headers().get("Retry-After");
                    if (vals != null && !vals.isEmpty()) {
                        String v = vals.iterator().next();
                        if (!v.isBlank() && v.chars().allMatch(Character::isDigit)) {
                            retryAfter = new Date(System.currentTimeMillis() + Long.parseLong(v) * 1000);
                        }
                    }
                }
                Request req = response.request();
                Request.HttpMethod method = (req != null) ? req.httpMethod() : null;
                String msg = "PG " + status + (body.isBlank() ? "" : (" " + body));
                return new RetryableException(status, msg, method, retryAfter, req);
            }

            // 그 외(주로 4xx)는 기본 Feign 예외 → 재시도 제외(Resilience4j에서 ignore)
            return feign.FeignException.errorStatus(methodKey, response);
        };
    }


//    @Bean
//    public ErrorDecoder pgErrorDecoder() {
//        return (methodKey, response) -> {
//            int status = response.status();
//
//            String body = "";
//            try {
//                if (response.body() != null) {
//                    body = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
//                    if (body.length() > 500) body = body.substring(0, 500) + " …";
//                }
//            } catch (IOException ignored) { }
//
//            if (status == 408 || status == 429 || status >= 500) {
//                Date retryAfter = null;
//                List<String> vals = response.headers() != null ? response.headers().get("Retry-After") : null;
//                if (vals != null && !vals.isEmpty()) {
//                    String v = vals.get(0);
//                    if (v != null && v.matches("\\d+")) {
//                        retryAfter = new Date(System.currentTimeMillis() + Long.parseLong(v) * 1000L);
//                    }
//                }
//                Request req = response.request();
//                return new RetryableException(
//                        status,
//                        "PG " + status + (body.isEmpty() ? "" : " " + body),
//                        req != null ? req.httpMethod() : null,
//                        retryAfter,
//                        req
//                );
//            }
//
//            return FeignException.errorStatus(methodKey, response);
//        };
//    }
}
