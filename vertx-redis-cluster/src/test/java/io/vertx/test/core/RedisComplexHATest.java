package io.vertx.test.core;

import com.hiddenswitch.containers.RedisContainer;
import io.vertx.core.ComplexHATest;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.redis.RedisClusterManager;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;


public class RedisComplexHATest extends ComplexHATest {

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
		return new RedisClusterManager(redisContainer.getRedisUrl(), 1);
	}

	@Override
	@Ignore("do not repeat")
	public void testComplexFailover() {
	}

	@Test
	public void testComplexFailover1() {
		try {
			int numNodes = 8;
			this.createNodes(numNodes);
			this.deployRandomVerticles(this::killRandom);
			this.await(2L, TimeUnit.MINUTES);
		} catch (Throwable var2) {
			var2.printStackTrace();
			this.fail(var2.getMessage());
		}
	}
}
