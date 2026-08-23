package hn.shadowcore.mercadox.library.redis.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public class RedisIdempotencyChecker {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "eventId:";

    /**
     * Atomically claims the eventId for processing.
     * Returns true if this is the first time this eventId is seen (caller should proceed).
     * Returns false if the eventId was already processed (caller should skip — duplicate).
     *
     * Using a single setIfAbsent eliminates the TOCTOU race that existed when
     * isDuplicate (GET) and markProcessed (SETNX) were separate operations.
     */
    public boolean claimProcessing(String eventId) {
        Boolean wasAbsent = redisTemplate.opsForValue()
                .setIfAbsent(getKey(eventId), "1", Duration.ofHours(24));
        return Boolean.TRUE.equals(wasAbsent);
    }

    private String getKey(String uuid) {
        return PREFIX + uuid;
    }

}
