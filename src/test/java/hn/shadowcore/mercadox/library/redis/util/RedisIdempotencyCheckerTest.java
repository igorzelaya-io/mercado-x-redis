package hn.shadowcore.mercadox.library.redis.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
    void isDuplicate_shouldReturnTrue_whenEventIdAlreadyExistsInRedis() {
        when(valueOperations.get("eventId:event-123")).thenReturn("true");

        boolean result = idempotencyChecker.isDuplicate("event-123");

        assertThat(result).isTrue();
    }

    @Test
    void isDuplicate_shouldReturnFalse_whenEventIdIsNotPresentInRedis() {
        when(valueOperations.get("eventId:event-123")).thenReturn(null);

        boolean result = idempotencyChecker.isDuplicate("event-123");

        assertThat(result).isFalse();
    }

    @Test
    void isDuplicate_shouldPrefixTheEventIdWhenBuildingTheKey() {
        idempotencyChecker.isDuplicate("event-123");

        verify(valueOperations).get("eventId:event-123");
    }

    // Null/blank IDs reach these methods only if the aspect's null guard fails.
    // The tests document the permissive fallthrough behaviour — not the happy path.
    @Test
    void isDuplicate_shouldHandleNullEventId() {
        when(valueOperations.get("eventId:null")).thenReturn(null);

        boolean result = idempotencyChecker.isDuplicate(null);

        assertThat(result).isFalse();
        verify(valueOperations).get("eventId:null");
    }

    @Test
    void isDuplicate_shouldHandleBlankEventId() {
        when(valueOperations.get("eventId:")).thenReturn(null);

        boolean result = idempotencyChecker.isDuplicate("");

        assertThat(result).isFalse();
        verify(valueOperations).get("eventId:");
    }

    @Test
    void markProcessed_shouldStoreKeyWithTwentyFourHourTtl() {
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

        idempotencyChecker.markProcessed("event-123");

        verify(valueOperations).setIfAbsent(
                eq("eventId:event-123"),
                eq(String.valueOf(Boolean.TRUE)),
                eq(Duration.ofHours(24)));
    }

    @Test
    void markProcessed_shouldNotThrow_whenKeyIsAlreadyPresent() {
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);

        assertThatCode(() -> idempotencyChecker.markProcessed("event-123"))
                .doesNotThrowAnyException();

        verify(valueOperations).setIfAbsent(
                eq("eventId:event-123"),
                eq(String.valueOf(Boolean.TRUE)),
                eq(Duration.ofHours(24)));
    }

    @Test
    void markProcessed_shouldHandleNullUuid() {
        idempotencyChecker.markProcessed(null);

        verify(valueOperations).setIfAbsent(
                eq("eventId:null"),
                eq(String.valueOf(Boolean.TRUE)),
                eq(Duration.ofHours(24)));
    }

    @Test
    void markProcessed_shouldNotInteractWithGet() {
        idempotencyChecker.markProcessed("event-123");

        verify(valueOperations, never()).get(any());
    }

    @Test
    void isDuplicate_shouldReturnTrue_afterStoredValueReflectsMarkProcessed() {
        // Simulates the full flow: markProcessed writes a key, isDuplicate finds it.
        // Mock-level wiring; see RedisIdempotencyCheckerIntTest for the real round-trip.
        String eventId = "event-lifecycle";
        when(valueOperations.get("eventId:" + eventId)).thenReturn("true");

        idempotencyChecker.markProcessed(eventId);
        boolean result = idempotencyChecker.isDuplicate(eventId);

        assertThat(result).isTrue();
        verify(valueOperations).setIfAbsent(eq("eventId:" + eventId), eq("true"), eq(Duration.ofHours(24)));
        verify(valueOperations).get("eventId:" + eventId);
    }
}
