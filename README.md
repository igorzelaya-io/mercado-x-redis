# MercadoX Redis

## Overview

`mercado-x-redis` is the shared Redis access layer for the MercadoX ecosystem. It provides connection/serialization configuration, cache TTL policy, and the storage primitives that other modules build higher-level features on — most notably the idempotency checks used by `mercado-x-context`.

It is a library, not a service — it auto-configures Redis beans into whichever Spring Boot application depends on it.

---

## Responsibilities

- Redis connection factory and template configuration (`RedisConfig`)
- Named cache TTL policy (`RedisTtlConfig`)
- Idempotency key storage (`RedisIdempotencyChecker`)
- Cart persistence (`CartRedisRepository`)

---

## What's In Here

### Connection & Serialization (`RedisConfig`)

Configures a Lettuce-based `RedisConnectionFactory` (`spring.data.redis.host`/`port`, defaulting to `localhost:6379`) and two templates for two different use cases:

- **`StringRedisTemplate`** — plain string keys/values, used for idempotency keys where the value is just a marker (`"1"`).
- **`RedisTemplate<String, Object>`** — `GenericJackson2JsonRedisSerializer` for values, used wherever a structured object needs to round-trip through Redis (e.g. `CartDto`).

Both are `@ConditionalOnMissingBean`, so a consuming service can supply its own if it needs different serialization behavior.

### Cache TTLs (`RedisTtlConfig`)

Named `RedisCacheManager` cache regions with explicit TTLs, consumed via `@Cacheable`/`@CacheEvict` in services:

| Cache name | TTL | Used for |
|---|---|---|
| `activeOrgs` | 1 hour | Organization lookups for active tenants |
| `inactiveOrgs` | 2 hours | Organization lookups for inactive tenants (cached longer — less likely to change) |
| `reviewOrders` | 1 hour | Orders in `UNDER_REVIEW` status |

### Idempotency Storage (`RedisIdempotencyChecker`)

Backs `mercado-x-context`'s `@KafkaIdempotent` aspect. Stores processed event IDs under `eventId:{id}` with a 24-hour TTL.

```java
public boolean isDuplicate(String eventId) {
    return redisTemplate.opsForValue().get(getKey(eventId)) != null;
}

public void markProcessed(String uuid) {
    redisTemplate.opsForValue().setIfAbsent(getKey(uuid), "true", Duration.ofHours(24));
}
```

**Known gap:** `isDuplicate` (GET) and `markProcessed` (SETNX) are two separate Redis round trips. Under concurrent delivery of the same event — a realistic scenario with Kafka's at-least-once guarantees — two threads can both pass the `isDuplicate` check before either calls `markProcessed`, letting the same event process twice. Collapsing this into a single `setIfAbsent`-first call (the pattern `mercado-x-context`'s API-level `IdempotencyAspect` already uses for `@IdempotentOperation`) would close the gap; tracked in `mercado-x-email`'s `TODO.md`.

The `@ConditionalOnBean(value = {StringRedisTemplate.class, RedisTemplate.class})` guard on this component is evaluated during component scanning, which runs *before* Spring Boot autoconfigures those Redis beans — so the condition can spuriously evaluate to `false` and leave the checker unregistered depending on bean initialization order. Also tracked in the same backlog; the fix is to switch to `@ConditionalOnClass` (a classpath check, not a bean-existence check) or promote this to a proper `@AutoConfiguration`.

### Cart Storage (`CartRedisRepository`)

Simple TTL-backed cart persistence — `cart:{userId}` → `CartDto`, 1-day expiry, no database round trip for what's inherently ephemeral, pre-checkout state.

---

## Configuration Reference

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

---

## Testing

Integration tests run against a real Redis via Testcontainers (`RedisTestSupport`, `TestRedisConnectionConfig`).

```bash
mvn test
```

---

## Internal Dependencies

| Module | Purpose |
|---|---|
| `mercado-x-library-entity` | `CartDto` and other types cached/stored through this module |

---

## Used By

- `mercado-x-context` (idempotency backing store)
- `mercado-x-oauth`
- `mercado-x-core`
