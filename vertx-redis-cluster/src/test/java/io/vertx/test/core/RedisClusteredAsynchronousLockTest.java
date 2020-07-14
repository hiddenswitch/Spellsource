package io.vertx.test.core;


import com.hiddenswitch.containers.RedisContainer;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.shareddata.ClusteredAsynchronousLockTest;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.redis.RedisClusterManager;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:ben@hiddenswitch.com">Benjamin Berman</a>
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class RedisClusteredAsynchronousLockTest extends ClusteredAsynchronousLockTest {

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
		// Run tests without graceful exit to demonstrate durability in case of a kill
		return new RedisClusterManager(redisContainer.getRedisUrl())
				.setExitGracefully(false);
	}

	/**
	 * From {@link #testLockReleased(Consumer)}
	 * <p>
	 * This cluster implementation correctly expires leases when nodes go down
	 *
	 * @param action
	 * @throws Exception
	 */
	private void testLockReleased1(Consumer<CountDownLatch> action) throws Exception {
		CountDownLatch lockAquiredLatch = new CountDownLatch(1);
		this.vertices[0].sharedData().getLockWithTimeout("pimpo", this.getLockTimeout(), this.onSuccess((lock) -> {
			this.vertices[1].sharedData().getLockWithTimeout("pimpo", this.getLockTimeout(), this.onSuccess((lock2) -> {
				this.testComplete();
			}));
			lockAquiredLatch.countDown();
		}));
		this.awaitLatch(lockAquiredLatch);
		CountDownLatch closeLatch = new CountDownLatch(1);
		action.accept(closeLatch);
		this.awaitLatch(closeLatch);
		this.await();
	}

	/**
	 * Tests locks for closed nodes, a behavior that works correctly in this cluster manager
	 *
	 * @throws Exception
	 */
	@Test
	public void testLockReleasedForClosedNode1() throws Exception {
		this.testLockReleased1((latch) -> {
			this.vertices[0].close(this.onSuccess((v) -> {
				latch.countDown();
			}));
		});
	}

	/**
	 * Tests locks for killed nodes, a behavior that works correctly in this cluster manager
	 *
	 * @throws Exception
	 */
	@Test
	public void testLockReleasedForKilledNode1() throws Exception {
		this.testLockReleased1((latch) -> {
			VertxInternal vi = (VertxInternal) this.vertices[0];
			vi.getClusterManager().leave(this.onSuccess((v) -> {
				latch.countDown();
			}));
		});
	}

}
