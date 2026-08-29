package hn.shadowcore.mercadox.library.redis.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final StringRedisTemplate redisTemplate;

    /**
     * Fixed-window counter: increments the key and opens its TTL only on the
     * first hit of the window. Accepts a small non-atomic race between
     * increment and expire (a key could in theory never get a TTL if the
     * process dies between the two calls) — acceptable for a spam-mitigation
     * control, not a security-critical one. An atomic Lua INCR+EXPIRE is the
     * upgrade path if this ever needs to be airtight.
     *
     * Returns true if the request is allowed (under the limit), false if the
     * limit for this key/window has already been exceeded.
     */
    public boolean tryConsume(String key, int maxRequests, Duration window) {
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null) {
            return true;
        }

        if (count == 1L) {
            redisTemplate.expire(key, window);
        }

        return count <= maxRequests;
    }

}
