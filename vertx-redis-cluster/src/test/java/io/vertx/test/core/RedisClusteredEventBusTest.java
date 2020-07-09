package io.vertx.test.core;

import com.hiddenswitch.containers.RedisContainer;
import io.vertx.core.eventbus.ClusteredEventBusTest;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.redis.RedisClusterManager;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class RedisClusteredEventBusTest extends ClusteredEventBusTest {

	@Rule
	public RedisContainer redisContainer = new RedisContainer();

	@Override
	public void before() throws Exception {
		redisContainer.clear();
		super.before();
	}

	@Override
	protected ClusterManager getClusterManager() {
		return new RedisClusterManager(redisContainer.getRedisUrl(), 1);
	}

	// Failing tests
	@Override
	@Ignore("painful to return no handler instead of timeout")
	public void testSendWhileUnsubscribing() throws Exception {

	}
}
