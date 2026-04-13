package hn.shadowcore.mercadox.library.redis.util;

import hn.shadowcore.mercadox.library.redis.base.BaseTestEntities;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class RedisTestSupport extends BaseTestEntities {

    @Container
    public static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

}