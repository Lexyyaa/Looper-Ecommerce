package com.loopers.config.redis;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;
import java.util.function.Consumer;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    public static final String CONNECTION_MASTER = "redisConnectionMaster";
    public static final String REDIS_TEMPLATE_MASTER = "redisTemplateMaster";

    private final RedisProperties redisProperties;

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Primary
    @Bean
    public LettuceConnectionFactory defaultRedisConnectionFactory() {
        return lettuceConnectionFactory(
                redisProperties.database(),
                redisProperties.master(),
                redisProperties.replicas(),
                builder -> builder.readFrom(ReadFrom.REPLICA_PREFERRED)
        );
    }

    @Qualifier(CONNECTION_MASTER)
    @Bean
    public LettuceConnectionFactory masterRedisConnectionFactory() {
        return lettuceConnectionFactory(
                redisProperties.database(),
                redisProperties.master(),
                redisProperties.replicas(),
                builder -> builder.readFrom(ReadFrom.MASTER)
        );
    }

    @Primary
    @Bean
    public RedisTemplate<String, String> defaultRedisTemplate(
            LettuceConnectionFactory lettuceConnectionFactory
    ) {
        return configureRedisTemplate(new RedisTemplate<>(), lettuceConnectionFactory);
    }

    @Qualifier(REDIS_TEMPLATE_MASTER)
    @Bean
    public RedisTemplate<String, String> masterRedisTemplate(
            @Qualifier(CONNECTION_MASTER) LettuceConnectionFactory lettuceConnectionFactory
    ) {
        return configureRedisTemplate(new RedisTemplate<>(), lettuceConnectionFactory);
    }

    private LettuceConnectionFactory lettuceConnectionFactory(
            int database,
            RedisNodeInfo master,
            List<RedisNodeInfo> replicas,
            Consumer<LettuceClientConfiguration.LettuceClientConfigurationBuilder> customizer
    ) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
        customizer.accept(builder);
        LettuceClientConfiguration lettuceClientConfiguration = builder.build();

        RedisStaticMasterReplicaConfiguration masterReplicaConfig =
                new RedisStaticMasterReplicaConfiguration(master.host(), master.port());
        masterReplicaConfig.setDatabase(database);

        replicas.forEach(replica -> masterReplicaConfig.addNode(replica.host(), replica.port()));

        return new LettuceConnectionFactory(masterReplicaConfig, lettuceClientConfiguration);
    }

    private <K, V> RedisTemplate<K, V> configureRedisTemplate(
            RedisTemplate<K, V> template,
            LettuceConnectionFactory connectionFactory
    ) {
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }
}
