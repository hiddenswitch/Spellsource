package io.vertx.test.core;

import com.hiddenswitch.containers.RedisContainer;
import io.vertx.core.shareddata.ClusteredSharedCounterTest;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.redis.RedisClusterManager;
import org.junit.Before;
import org.junit.ClassRule;

public class RedisClusteredSharedCounterTest extends ClusteredSharedCounterTest {

	@ClassRule
	public static RedisContainer redisContainer = new RedisContainer();

	@Override
	@Before
	public void before() throws Exception {
		redisContainer.clear();
		super.before();
	}

	@Override
	protected ClusterManager getClusterManager() {
		return new RedisClusterManager(redisContainer.getRedisUrl());
	}
}
