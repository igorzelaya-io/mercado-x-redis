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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisIdempotencyCheckerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisIdempotencyChecker idempotencyChecker;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        idempotencyChecker = new RedisIdempotencyChecker(redisTemplate);
    }

    @Test
    void claimProcessing_shouldReturnTrue_whenKeyIsAbsent() {
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

        boolean result = idempotencyChecker.claimProcessing("event-123");

        assertThat(result).isTrue();
    }

    @Test
    void claimProcessing_shouldReturnFalse_whenKeyAlreadyExists() {
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);

        boolean result = idempotencyChecker.claimProcessing("event-123");

        assertThat(result).isFalse();
    }

    @Test
    void claimProcessing_shouldReturnFalse_whenSetIfAbsentReturnsNull() {
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(null);

        boolean result = idempotencyChecker.claimProcessing("event-123");

        assertThat(result).isFalse();
    }

    @Test
    void claimProcessing_shouldPrefixEventIdWhenBuildingKey() {
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

        idempotencyChecker.claimProcessing("event-123");

        verify(valueOperations).setIfAbsent(eq("eventId:event-123"), any(), any(Duration.class));
    }

    @Test
    void claimProcessing_shouldStoreValue1_withTtlOf24Hours() {
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

        idempotencyChecker.claimProcessing("event-123");

        verify(valueOperations).setIfAbsent(
                eq("eventId:event-123"),
                eq("1"),
                eq(Duration.ofHours(24)));
    }

    // Null/blank IDs reach this method only if the aspect's null guard fails.
    // The tests document the permissive fallthrough behaviour — not the happy path.
    @Test
    void claimProcessing_shouldHandleNullEventId() {
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

        boolean result = idempotencyChecker.claimProcessing(null);

        assertThat(result).isTrue();
        verify(valueOperations).setIfAbsent(eq("eventId:null"), eq("1"), eq(Duration.ofHours(24)));
    }

    @Test
    void claimProcessing_shouldHandleBlankEventId() {
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

        boolean result = idempotencyChecker.claimProcessing("");

        assertThat(result).isTrue();
        verify(valueOperations).setIfAbsent(eq("eventId:"), eq("1"), eq(Duration.ofHours(24)));
    }

    @Test
    void claimProcessing_shouldNeverCallGet() {
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

        idempotencyChecker.claimProcessing("event-123");

        verify(valueOperations, never()).get(any());
    }
}
