package io.vertx.test.core;

import com.hiddenswitch.containers.RedisContainer;
import io.vertx.core.eventbus.ClusteredEventBusTest;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.redis.RedisClusterManager;
import org.junit.ClassRule;

public class RedisClusteredEventBusTest extends ClusteredEventBusTest {

	@ClassRule
	public static RedisContainer redisContainer = new RedisContainer();

	@Override
	public void before() throws Exception {
		super.before();
	}

	@Override
	protected ClusterManager getClusterManager() {
		return new RedisClusterManager(redisContainer.getRedisUrl(), 1)
				.setExitGracefully(false);
	}
}
