package hn.alturaforge.mercadox.library.redis.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRateLimiterTest {

    private static final String KEY = "leadRate:ip:127.0.0.1";
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rateLimiter = new RedisRateLimiter(redisTemplate);
    }

    @Test
    void unavailableCounterFailsOpenWithoutCreatingTtl() {
        when(valueOperations.increment(KEY)).thenReturn(null);

        assertThat(rateLimiter.tryConsume(KEY, 3, WINDOW)).isTrue();

        verify(redisTemplate, never()).expire(KEY, WINDOW);
    }

    @Test
    void firstRequestOpensWindowAndIsAllowed() {
        when(valueOperations.increment(KEY)).thenReturn(1L);

        assertThat(rateLimiter.tryConsume(KEY, 3, WINDOW)).isTrue();

        verify(redisTemplate).expire(KEY, WINDOW);
    }

    @Test
    void requestWithinExistingWindowDoesNotResetTtl() {
        when(valueOperations.increment(KEY)).thenReturn(3L);

        assertThat(rateLimiter.tryConsume(KEY, 3, WINDOW)).isTrue();

        verify(redisTemplate, never()).expire(KEY, WINDOW);
    }

    @Test
    void requestAboveLimitIsRejectedWithoutResettingTtl() {
        when(valueOperations.increment(KEY)).thenReturn(4L);

        assertThat(rateLimiter.tryConsume(KEY, 3, WINDOW)).isFalse();

        verify(redisTemplate, never()).expire(KEY, WINDOW);
    }
}
