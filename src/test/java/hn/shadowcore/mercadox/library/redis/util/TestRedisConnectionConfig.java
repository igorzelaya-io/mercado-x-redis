package hn.shadowcore.mercadox.library.redis.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class TestRedisConnectionConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(
                RedisTestSupport.redis.getHost(),
                RedisTestSupport.redis.getMappedPort(6379)
        );
    }
}