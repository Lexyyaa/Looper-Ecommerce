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

            String body = "";
            try {
                if (response.body() != null) {
                    String s = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    body = s.length() > 200 ? s.substring(0, 200) + "…" : s;
                }
            } catch (IOException ignored) {}

            if (status == 408 || status == 429 || status >= 500) {
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

            return feign.FeignException.errorStatus(methodKey, response);
        };
    }
}
