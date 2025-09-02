package com.loopers.testcontainers;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class RedisTestContainersConfig {

    private static final GenericContainer<?> redisContainer;

    static {
        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                .withExposedPorts(6379);
        redisContainer.start();
    }

    {
        System.setProperty("datasource.redis.database", "0");
        System.setProperty("datasource.redis.master.host", redisContainer.getHost());

        System.setProperty("datasource.redis.master.port", String.valueOf(redisContainer.getFirstMappedPort()));
        System.setProperty("datasource.redis.replicas[0].host", redisContainer.getHost());
        System.setProperty("datasource.redis.replicas[0].port", String.valueOf(redisContainer.getFirstMappedPort()));
    }
}
