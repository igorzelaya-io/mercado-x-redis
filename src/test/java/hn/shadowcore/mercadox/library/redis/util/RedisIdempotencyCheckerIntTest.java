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
    void claimProcessing_shouldReturnTrue_forUnseenEventId() {
        String eventId = UUID.randomUUID().toString();

        assertThat(idempotencyChecker.claimProcessing(eventId)).isTrue();
    }

    @Test
    void claimProcessing_shouldReturnFalse_onSecondCallForSameId() {
        String eventId = UUID.randomUUID().toString();

        idempotencyChecker.claimProcessing(eventId);

        assertThat(idempotencyChecker.claimProcessing(eventId)).isFalse();
    }

    @Test
    void claimProcessing_shouldStoreValue1UnderPrefixedKey() {
        String eventId = UUID.randomUUID().toString();

        idempotencyChecker.claimProcessing(eventId);

        String stored = stringRedisTemplate.opsForValue().get("eventId:" + eventId);
        assertThat(stored).isEqualTo("1");
    }

    @Test
    void claimProcessing_shouldApplyTtlOf24Hours() {
        String eventId = UUID.randomUUID().toString();

        idempotencyChecker.claimProcessing(eventId);

        Long ttlSeconds = stringRedisTemplate.getExpire("eventId:" + eventId, TimeUnit.SECONDS);
        assertThat(ttlSeconds)
                .isGreaterThan(Duration.ofHours(23).toSeconds())
                .isLessThanOrEqualTo(Duration.ofHours(24).toSeconds());
    }

    @Test
    void claimProcessing_secondCallShouldReturnFalse_andKeyShouldStillExist() {
        String eventId = UUID.randomUUID().toString();

        idempotencyChecker.claimProcessing(eventId);
        boolean secondResult = idempotencyChecker.claimProcessing(eventId);

        assertThat(secondResult).isFalse();
        assertThat(stringRedisTemplate.hasKey("eventId:" + eventId)).isTrue();
    }

    @Test
    void claimProcessing_shouldNotAffectOtherEventIds() {
        String claimedId = UUID.randomUUID().toString();
        String otherId = UUID.randomUUID().toString();

        idempotencyChecker.claimProcessing(claimedId);

        assertThat(idempotencyChecker.claimProcessing(otherId)).isTrue();
    }

    @Test
    void claimProcessing_secondCallShouldNotResetTtl() {
        String eventId = UUID.randomUUID().toString();

        idempotencyChecker.claimProcessing(eventId);
        Long ttlAfterFirst = stringRedisTemplate.getExpire("eventId:" + eventId, TimeUnit.SECONDS);

        // Second call is a no-op via setIfAbsent (NX) — TTL must not be extended or reset
        idempotencyChecker.claimProcessing(eventId);
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
