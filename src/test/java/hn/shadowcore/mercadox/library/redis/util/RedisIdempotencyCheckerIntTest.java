package hn.shadowcore.mercadox.library.redis.util;

import hn.shadowcore.mercadox.library.redis.config.RedisConfig;
import hn.shadowcore.mercadox.library.redis.config.RedisTtlConfig;
import hn.shadowcore.mercadox.library.redis.repository.CartRedisRepository;
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
import static org.assertj.core.api.Assertions.assertThatCode;

@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CartRedisRepository.class,
        TestRedisConnectionConfig.class,
        RedisConfig.class,
        RedisTtlConfig.class,
        RedisIdempotencyCheckerIntTest.TestConfig.class
})
class RedisIdempotencyCheckerIntTest extends RedisTestSupport {

    @Autowired
    private RedisIdempotencyChecker idempotencyChecker;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void isDuplicate_shouldReturnFalse_forUnseenEventId() {
        String eventId = UUID.randomUUID().toString();

        assertThat(idempotencyChecker.isDuplicate(eventId)).isFalse();
    }

    @Test
    void markProcessed_thenIsDuplicate_shouldReturnTrue() {
        String eventId = UUID.randomUUID().toString();

        idempotencyChecker.markProcessed(eventId);

        assertThat(idempotencyChecker.isDuplicate(eventId)).isTrue();
    }

    @Test
    void markProcessed_shouldStoreValueUnderPrefixedKey() {
        String eventId = UUID.randomUUID().toString();

        idempotencyChecker.markProcessed(eventId);

        String stored = stringRedisTemplate.opsForValue().get("eventId:" + eventId);
        assertThat(stored).isEqualTo("true");
    }

    @Test
    void markProcessed_shouldApplyTtlOf24Hours() {
        String eventId = UUID.randomUUID().toString();

        idempotencyChecker.markProcessed(eventId);

        Long ttlSeconds = stringRedisTemplate.getExpire("eventId:" + eventId, TimeUnit.SECONDS);
        assertThat(ttlSeconds)
                .isGreaterThan(Duration.ofHours(23).toSeconds())
                .isLessThanOrEqualTo(Duration.ofHours(24).toSeconds());
    }

    @Test
    void markProcessed_shouldBeIdempotent_secondCallDoesNotThrowAndKeyStillPresent() {
        String eventId = UUID.randomUUID().toString();

        idempotencyChecker.markProcessed(eventId);

        assertThatCode(() -> idempotencyChecker.markProcessed(eventId))
                .doesNotThrowAnyException();

        assertThat(idempotencyChecker.isDuplicate(eventId)).isTrue();
    }

    @Test
    void markProcessed_shouldNotAffectOtherEventIds() {
        String markedId = UUID.randomUUID().toString();
        String otherId = UUID.randomUUID().toString();

        idempotencyChecker.markProcessed(markedId);

        assertThat(idempotencyChecker.isDuplicate(otherId)).isFalse();
    }

    @Test
    void markProcessed_shouldNotResetTtl_onSecondCall() {
        String eventId = UUID.randomUUID().toString();

        idempotencyChecker.markProcessed(eventId);
        Long ttlAfterFirst = stringRedisTemplate.getExpire("eventId:" + eventId, TimeUnit.SECONDS);

        // Second call is a no-op via setIfAbsent — TTL must not be extended or reset
        idempotencyChecker.markProcessed(eventId);
        Long ttlAfterSecond = stringRedisTemplate.getExpire("eventId:" + eventId, TimeUnit.SECONDS);

        assertThat(ttlAfterSecond).isLessThanOrEqualTo(ttlAfterFirst);
    }

    @Configuration
    static class TestConfig {

        @Bean
        public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
            return new StringRedisTemplate(factory);
        }

        @Bean
        public RedisIdempotencyChecker redisIdempotencyChecker(StringRedisTemplate stringRedisTemplate) {
            return new RedisIdempotencyChecker(stringRedisTemplate);
        }
    }
}
