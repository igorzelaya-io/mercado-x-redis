# Graph Report - mercado-x-redis  (2026-09-08)

## Corpus Check
- 19 files · ~2,483 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 150 nodes · 293 edges · 12 communities (9 shown, 3 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 26 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `de2af28d`
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
- AGENTS.md
- CLAUDE.md

## God Nodes (most connected - your core abstractions)
1. `RedisIdempotencyCheckerIntTest` - 15 edges
2. `RedisIdempotencyCheckerTest` - 14 edges
3. `RedisRateLimiterIntTest` - 13 edges
4. `RedisIdempotencyChecker` - 12 edges
5. `CartRedisRepositoryIntTest` - 12 edges
6. `CartRedisRepository` - 11 edges
7. `RedisConfig` - 10 edges
8. `BaseTestEntities` - 10 edges
9. `RedisRateLimiter` - 9 edges
10. `MercadoX Redis` - 8 edges

## Surprising Connections (you probably didn't know these)
- `RedisIdempotencyCheckerIntTest` --references--> `RedisIdempotencyChecker`  [EXTRACTED]
  src/test/java/hn/shadowcore/mercadox/library/redis/util/RedisIdempotencyCheckerIntTest.java → src/main/java/hn/shadowcore/mercadox/library/redis/util/RedisIdempotencyChecker.java
- `CartRedisRepositoryIntTest` --inherits--> `RedisTestSupport`  [EXTRACTED]
  src/test/java/hn/shadowcore/mercadox/library/redis/repository/CartRedisRepositoryIntTest.java → src/test/java/hn/shadowcore/mercadox/library/redis/util/RedisTestSupport.java
- `RedisIdempotencyCheckerIntTest` --inherits--> `RedisTestSupport`  [EXTRACTED]
  src/test/java/hn/shadowcore/mercadox/library/redis/util/RedisIdempotencyCheckerIntTest.java → src/test/java/hn/shadowcore/mercadox/library/redis/util/RedisTestSupport.java
- `RedisRateLimiterIntTest` --inherits--> `RedisTestSupport`  [EXTRACTED]
  src/test/java/hn/shadowcore/mercadox/library/redis/util/RedisRateLimiterIntTest.java → src/test/java/hn/shadowcore/mercadox/library/redis/util/RedisTestSupport.java
- `CartRedisRepositoryIntTest` --references--> `CartRedisRepository`  [EXTRACTED]
  src/test/java/hn/shadowcore/mercadox/library/redis/repository/CartRedisRepositoryIntTest.java → src/main/java/hn/shadowcore/mercadox/library/redis/repository/CartRedisRepository.java

## Import Cycles
- None detected.

## Communities (12 total, 3 thin omitted)

### Community 0 - "RedisIdempotencyCheckerIntTest"
Cohesion: 0.19
Nodes (10): Bean, Configuration, ContextConfiguration, ExtendWith, RedisConnectionFactory, StringRedisTemplate, Test, Testcontainers (+2 more)

### Community 1 - ".claimProcessing"
Cohesion: 0.19
Nodes (10): RequiredArgsConstructor, Slf4j, StringRedisTemplate, RedisIdempotencyChecker, BeforeEach, ExtendWith, StringRedisTemplate, Test (+2 more)

### Community 2 - "RedisConfig.java"
Cohesion: 0.28
Nodes (10): ComponentScan, ConditionalOnMissingBean, EnableCaching, RedisCacheConfiguration, Bean, Configuration, RedisConnectionFactory, RedisTemplate (+2 more)

### Community 3 - "CartRedisRepository"
Cohesion: 0.18
Nodes (13): ItemDto, Repository, CartRedisRepository, CartDto, RedisTemplate, RequiredArgsConstructor, CartRedisRepositoryIntTest, BeforeEach (+5 more)

### Community 4 - "BaseTestEntities"
Cohesion: 0.30
Nodes (6): GenericContainer, Order, Organization, BaseTestEntities, RedisTestSupport, User

### Community 5 - "CartRedisRepositoryIntTest.java"
Cohesion: 0.17
Nodes (14): RequiredArgsConstructor, Slf4j, StringRedisTemplate, RedisRateLimiter, Bean, Configuration, ContextConfiguration, ExtendWith (+6 more)

### Community 6 - "RedisIdempotencyChecker"
Cohesion: 0.15
Nodes (12): Cache TTLs (`RedisTtlConfig`), Cart Storage (`CartRedisRepository`), Configuration Reference, Connection & Serialization (`RedisConfig`), Idempotency Storage (`RedisIdempotencyChecker`), Internal Dependencies, MercadoX Redis, Overview (+4 more)

### Community 7 - "RedisTtlConfig.java"
Cohesion: 0.48
Nodes (5): CacheManager, Bean, Configuration, RedisConnectionFactory, RedisTtlConfig

### Community 8 - "TestRedisConnectionConfig.java"
Cohesion: 0.53
Nodes (4): Bean, Configuration, RedisConnectionFactory, TestRedisConnectionConfig

## Knowledge Gaps
- **13 isolated node(s):** `hn.shadowcore:mercado-x-redis`, `graphify`, `graphify`, `Overview`, `Responsibilities` (+8 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **3 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `RedisIdempotencyChecker` connect `.claimProcessing` to `RedisIdempotencyCheckerIntTest`, `RedisConfig.java`?**
  _High betweenness centrality (0.203) - this node is a cross-community bridge._
- **Why does `RedisTestSupport` connect `BaseTestEntities` to `RedisIdempotencyCheckerIntTest`, `CartRedisRepository`, `CartRedisRepositoryIntTest.java`?**
  _High betweenness centrality (0.177) - this node is a cross-community bridge._
- **Why does `RedisIdempotencyCheckerIntTest` connect `RedisIdempotencyCheckerIntTest` to `.claimProcessing`, `BaseTestEntities`?**
  _High betweenness centrality (0.142) - this node is a cross-community bridge._
- **What connects `hn.shadowcore:mercado-x-redis`, `graphify`, `graphify` to the rest of the system?**
  _13 weakly-connected nodes found - possible documentation gaps or missing edges._