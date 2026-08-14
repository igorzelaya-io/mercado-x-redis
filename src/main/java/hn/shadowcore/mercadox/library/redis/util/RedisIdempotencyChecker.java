package hn.shadowcore.mercadox.library.redis.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(value = {StringRedisTemplate.class, RedisTemplate.class})
public class RedisIdempotencyChecker {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "eventId:";

    public boolean isDuplicate(String eventId) {
         Optional<String> response = Optional
                 .ofNullable(redisTemplate.opsForValue().get(getKey(eventId)));
         return response.isPresent();
    }

    public void markProcessed(String uuid) {
        redisTemplate.opsForValue().setIfAbsent(getKey(uuid), String.valueOf(Boolean.TRUE),
                Duration.ofHours(24));
    }

    private String getKey(String uuid) {
        return PREFIX + uuid;
    }

}
