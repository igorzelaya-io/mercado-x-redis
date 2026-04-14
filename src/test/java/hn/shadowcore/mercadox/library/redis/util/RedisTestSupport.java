package hn.shadowcore.mercadox.library.redis.util;

import hn.shadowcore.mercadox.library.redis.base.BaseTestEntities;
import org.testcontainers.containers.GenericContainer;

public abstract class RedisTestSupport extends BaseTestEntities {

    public static final GenericContainer<?> redis;

    static {
        redis = new GenericContainer<>("redis:7-alpine")
                .withExposedPorts(6379);
        redis.start();
    }
}