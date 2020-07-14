package io.vertx.test.core;

import com.hiddenswitch.containers.RedisContainer;
import io.vertx.core.eventbus.ClusteredEventBusTest;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.redis.RedisClusterManager;
import org.junit.Rule;

public class RedisGracefulClusteredEventBusTest extends ClusteredEventBusTest {

	@Rule
	public RedisContainer redisContainer = new RedisContainer();

	@Override
	public void before() throws Exception {
		redisContainer.clear();
		super.before();
	}

	@Override
	protected ClusterManager getClusterManager() {
		return new RedisClusterManager(redisContainer.getRedisUrl(), 1)
				.setExitGracefully(true);
	}
}
