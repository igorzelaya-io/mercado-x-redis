package hn.alturaforge.mercadox.library.redis.util;

import hn.alturaforge.mercadox.library.redis.config.RedisConfig;
import hn.alturaforge.mercadox.library.redis.config.RedisTtlConfig;
import hn.alturaforge.mercadox.library.redis.repository.CartRedisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CartRedisRepository.class,
        TestRedisConnectionConfig.class,
        RedisConfig.class,
        RedisTtlConfig.class,
        RedisRateLimiterIntTest.TestConfig.class
})
class RedisRateLimiterIntTest extends RedisTestSupport {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    @Autowired
    private RedisRateLimiter rateLimiter;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void tryConsume_shouldReturnTrue_forFirstNCallsWithinLimit() {
        String key = "leadRate:ip:" + UUID.randomUUID();

        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertThat(rateLimiter.tryConsume(key, MAX_REQUESTS, WINDOW)).isTrue();
        }
    }

    @Test
    void tryConsume_shouldReturnFalse_onCallExceedingLimit() {
        String key = "leadRate:ip:" + UUID.randomUUID();

        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryConsume(key, MAX_REQUESTS, WINDOW);
        }

        assertThat(rateLimiter.tryConsume(key, MAX_REQUESTS, WINDOW)).isFalse();
    }

    @Test
    void tryConsume_shouldApplyTtlOnFirstCall() {
        String key = "leadRate:ip:" + UUID.randomUUID();

        rateLimiter.tryConsume(key, MAX_REQUESTS, WINDOW);

        Long ttlSeconds = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertThat(ttlSeconds)
                .isGreaterThan(0L)
                .isLessThanOrEqualTo(WINDOW.toSeconds());
    }

    @Test
    void tryConsume_secondCallShouldNotResetTtl() {
        String key = "leadRate:ip:" + UUID.randomUUID();

        rateLimiter.tryConsume(key, MAX_REQUESTS, WINDOW);
        Long ttlAfterFirst = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);

        rateLimiter.tryConsume(key, MAX_REQUESTS, WINDOW);
        Long ttlAfterSecond = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);

        assertThat(ttlAfterSecond).isLessThanOrEqualTo(ttlAfterFirst);
    }

    @Test
    void tryConsume_shouldNotAffectOtherKeys() {
        String consumedKey = "leadRate:ip:" + UUID.randomUUID();
        String otherKey = "leadRate:ip:" + UUID.randomUUID();

        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryConsume(consumedKey, MAX_REQUESTS, WINDOW);
        }
        rateLimiter.tryConsume(consumedKey, MAX_REQUESTS, WINDOW);

        assertThat(rateLimiter.tryConsume(otherKey, MAX_REQUESTS, WINDOW)).isTrue();
    }

    @Configuration
    static class TestConfig {

        @Bean
        public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
            return new StringRedisTemplate(factory);
        }

        @Bean
        public RedisRateLimiter redisRateLimiter(StringRedisTemplate stringRedisTemplate) {
            return new RedisRateLimiter(stringRedisTemplate);
        }
    }
}
