package com.hiddenswitch.spellsource;

import co.paralleluniverse.strands.Strand;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hiddenswitch.spellsource.concurrent.SuspendableLock;
import com.hiddenswitch.spellsource.concurrent.SuspendableQueue;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

public class ClusterTest extends SpellsourceTestBase {

	@Test
	@Ignore
	public void testLockOverCluster(TestContext context) {
		Async latch = context.async(3);
		AtomicReference<Vertx> newVertx = new AtomicReference<>();
		HazelcastInstance instance = Hazelcast.newHazelcastInstance(Cluster.getConfig(5702));
		Vertx.clusteredVertx(new VertxOptions()
				.setClusterManager(new HazelcastClusterManager(instance))
				.setWorkerPoolSize(99)
				.setInternalBlockingPoolSize(99)
				.setBlockedThreadCheckInterval(30000L)
				.setWarningExceptionTime(30000L), context.asyncAssertSuccess(newVertxInstance -> {
			// Deploy a second gateway
			newVertx.set(newVertxInstance);
			Mongo.mongo().connectWithEnvironment(newVertxInstance);

			// Connect to existing cluster
			vertx.runOnContext(v1 -> {
				for (int i = 0; i < 16; i++) {
					int finalI = i;
					vertx.runOnContext(suspendableHandler(v2 -> {
						SuspendableLock lock = SuspendableLock.lock("test" + Integer.toString(finalI));
						Strand.sleep(10000L);
						lock.release();
						latch.countDown();
					}));
				}

				for (int i = 16; i < 32; i++) {
					int finalI = i;
					vertx.runOnContext(suspendableHandler(v2 -> {
						Strand.sleep(2000L);
						SuspendableLock lock = SuspendableLock.lock("test" + Integer.toString(finalI));
						lock.release();
						latch.countDown();
					}));
				}
			});

			newVertxInstance.getOrCreateContext().runOnContext(v1 -> {
				for (int i = 0; i < 16; i++) {
					int finalI = i;
					vertx.runOnContext(suspendableHandler(v2 -> {
						SuspendableLock lock = SuspendableLock.lock("test" + Integer.toString(finalI));
					}));
				}
			});
		}));

		latch.awaitSuccess();
		newVertx.get().close(context.asyncAssertSuccess(v1 -> {
			instance.shutdown();
		}));
	}

	@Test
	public void testArrayQueueOverCluster(TestContext context) {
		Async latch = context.async(3);
		AtomicReference<Vertx> newVertx = new AtomicReference<>();
		HazelcastInstance instance = Hazelcast.newHazelcastInstance(Cluster.getConfig(5702));
		Vertx.clusteredVertx(new VertxOptions()
				.setClusterManager(new HazelcastClusterManager(instance))
				.setWorkerPoolSize(99)
				.setInternalBlockingPoolSize(99)
				.setBlockedThreadCheckInterval(30000L)
				.setWarningExceptionTime(30000L), context.asyncAssertSuccess(newVertxInstance -> {
			// Deploy a second gateway
			newVertx.set(newVertxInstance);
			Mongo.mongo().connectWithEnvironment(newVertxInstance);

			// Connect to existing cluster
			vertx.runOnContext(v1 -> {
				vertx.runOnContext(suspendableHandler(v2 -> {
					SuspendableQueue<String> queue = SuspendableQueue.get("test-1000");
					queue.offer("ok");
					Strand.sleep(5000L);
					queue.offer("ok2");
					Strand.sleep(1000L);
					String ok3 = queue.take();
					context.assertEquals(ok3, "ok3");
					latch.countDown();
				}));
			});

			newVertxInstance.getOrCreateContext().runOnContext(v2 -> {
				newVertxInstance.runOnContext(suspendableHandler(v3 -> {
					SuspendableQueue<String> queue = SuspendableQueue.get("test-1000");
					String ok = queue.take();
					context.assertEquals(ok, "ok");
					latch.countDown();
					String ok2 = queue.take();
					context.assertEquals(ok2, "ok2");
					latch.countDown();
					queue.offer("ok3");
				}));
			});
		}));

		latch.awaitSuccess();
		newVertx.get().close(context.asyncAssertSuccess(v1 -> {
			instance.shutdown();
		}));
	}

	@Test(timeout = 60000L)
	public void testMultiHostMultiClientCluster(TestContext context) {
		// Connect to existing cluster
		Async latch = context.async(10);
		AtomicReference<Vertx> newVertx = new AtomicReference<>();
		HazelcastInstance instance = Hazelcast.newHazelcastInstance(Cluster.getConfig(5702));
		Vertx.clusteredVertx(new VertxOptions()
				.setClusterManager(new HazelcastClusterManager(instance))
				.setBlockedThreadCheckInterval(30000L)
				.setWarningExceptionTime(30000L), context.asyncAssertSuccess(newVertxInstance -> {
			// Deploy a second gateway
			newVertx.set(newVertxInstance);
			Mongo.mongo().connectWithEnvironment(newVertxInstance);
			newVertxInstance.deployVerticle(Gateway.create(9090), context.asyncAssertSuccess(v2 -> {
				// Distribute clients to the two gateways
				Stream.generate(() -> Stream.of(8080, 9090)).flatMap(Function.identity())
						.map(port -> new Thread(() -> {
							UnityClient client = new UnityClient(context, port);
							client.createUserAccount();
							client.matchmakeConstructedPlay(null);
							client.waitUntilDone();
							context.assertTrue(client.isGameOver());
							client.disconnect();
							latch.countDown();
						})).limit(10).forEach(Thread::start);
			}));
		}));
		latch.awaitSuccess();
		newVertx.get().close(context.asyncAssertSuccess(v1 -> {
			instance.shutdown();
		}));
	}
}
