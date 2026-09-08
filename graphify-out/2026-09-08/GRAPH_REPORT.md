# Graph Report - .  (2026-08-21)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 108 nodes · 218 edges · 10 communities (9 shown, 1 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 21 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1a28ac89`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- RedisIdempotencyCheckerIntTest
- .claimProcessing
- RedisConfig.java
- CartRedisRepository
- BaseTestEntities
- CartRedisRepositoryIntTest.java
- RedisIdempotencyChecker
- RedisTtlConfig.java
- TestRedisConnectionConfig.java
- hn.shadowcore:mercado-x-redis

## God Nodes (most connected - your core abstractions)
1. `RedisIdempotencyCheckerIntTest` - 15 edges
2. `RedisIdempotencyCheckerTest` - 14 edges
3. `RedisIdempotencyChecker` - 12 edges
4. `CartRedisRepositoryIntTest` - 12 edges
5. `CartRedisRepository` - 10 edges
6. `BaseTestEntities` - 10 edges
7. `RedisConfig` - 7 edges
8. `RedisTestSupport` - 6 edges
9. `TestConfig` - 4 edges
10. `RedisTtlConfig` - 3 edges

## Surprising Connections (you probably didn't know these)
- `CartRedisRepositoryIntTest` --references--> `CartRedisRepository`  [EXTRACTED]
  src/test/java/hn/shadowcore/mercadox/library/redis/repository/CartRedisRepositoryIntTest.java → src/main/java/hn/shadowcore/mercadox/library/redis/repository/CartRedisRepository.java
- `RedisIdempotencyCheckerIntTest` --references--> `RedisIdempotencyChecker`  [EXTRACTED]
  src/test/java/hn/shadowcore/mercadox/library/redis/util/RedisIdempotencyCheckerIntTest.java → src/main/java/hn/shadowcore/mercadox/library/redis/util/RedisIdempotencyChecker.java
- `RedisIdempotencyCheckerTest` --references--> `RedisIdempotencyChecker`  [EXTRACTED]
  src/test/java/hn/shadowcore/mercadox/library/redis/util/RedisIdempotencyCheckerTest.java → src/main/java/hn/shadowcore/mercadox/library/redis/util/RedisIdempotencyChecker.java
- `CartRedisRepositoryIntTest` --inherits--> `RedisTestSupport`  [EXTRACTED]
  src/test/java/hn/shadowcore/mercadox/library/redis/repository/CartRedisRepositoryIntTest.java → src/test/java/hn/shadowcore/mercadox/library/redis/util/RedisTestSupport.java
- `RedisIdempotencyCheckerIntTest` --inherits--> `RedisTestSupport`  [EXTRACTED]
  src/test/java/hn/shadowcore/mercadox/library/redis/util/RedisIdempotencyCheckerIntTest.java → src/test/java/hn/shadowcore/mercadox/library/redis/util/RedisTestSupport.java

## Import Cycles
- None detected.

## Communities (10 total, 1 thin omitted)

### Community 0 - "RedisIdempotencyCheckerIntTest"
Cohesion: 0.19
Nodes (10): Bean, Configuration, ContextConfiguration, ExtendWith, RedisConnectionFactory, StringRedisTemplate, Test, Testcontainers (+2 more)

### Community 1 - ".claimProcessing"
Cohesion: 0.26
Nodes (6): BeforeEach, ExtendWith, StringRedisTemplate, Test, RedisIdempotencyCheckerTest, ValueOperations

### Community 2 - "RedisConfig.java"
Cohesion: 0.31
Nodes (9): ConditionalOnMissingBean, EnableCaching, RedisCacheConfiguration, Bean, Configuration, RedisConnectionFactory, RedisTemplate, StringRedisTemplate (+1 more)

### Community 3 - "CartRedisRepository"
Cohesion: 0.31
Nodes (6): Repository, CartRedisRepository, CartDto, RedisTemplate, RequiredArgsConstructor, Test

### Community 4 - "BaseTestEntities"
Cohesion: 0.30
Nodes (6): GenericContainer, Order, Organization, BaseTestEntities, RedisTestSupport, User

### Community 5 - "CartRedisRepositoryIntTest.java"
Cohesion: 0.38
Nodes (7): ItemDto, CartRedisRepositoryIntTest, BeforeEach, CartDto, ContextConfiguration, ExtendWith, Testcontainers

### Community 6 - "RedisIdempotencyChecker"
Cohesion: 0.43
Nodes (6): Component, ConditionalOnBean, Slf4j, RequiredArgsConstructor, StringRedisTemplate, RedisIdempotencyChecker

### Community 7 - "RedisTtlConfig.java"
Cohesion: 0.48
Nodes (5): CacheManager, Bean, Configuration, RedisConnectionFactory, RedisTtlConfig

### Community 8 - "TestRedisConnectionConfig.java"
Cohesion: 0.53
Nodes (4): Bean, Configuration, RedisConnectionFactory, TestRedisConnectionConfig

## Knowledge Gaps
- **1 isolated node(s):** `hn.shadowcore:mercado-x-redis`
  These have ≤1 connection - possible missing edges or undocumented components.
- **1 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `RedisIdempotencyCheckerIntTest` connect `RedisIdempotencyCheckerIntTest` to `BaseTestEntities`, `RedisIdempotencyChecker`?**
  _High betweenness centrality (0.398) - this node is a cross-community bridge._
- **Why does `RedisTestSupport` connect `BaseTestEntities` to `RedisIdempotencyCheckerIntTest`, `CartRedisRepositoryIntTest.java`?**
  _High betweenness centrality (0.294) - this node is a cross-community bridge._
- **Why does `RedisIdempotencyChecker` connect `RedisIdempotencyChecker` to `RedisIdempotencyCheckerIntTest`, `.claimProcessing`?**
  _High betweenness centrality (0.245) - this node is a cross-community bridge._
- **What connects `hn.shadowcore:mercado-x-redis` to the rest of the system?**
  _1 weakly-connected nodes found - possible documentation gaps or missing edges._