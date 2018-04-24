package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Suspendable;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hiddenswitch.spellsource.impl.*;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.core.*;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hiddenswitch.spellsource.util.Logging.setLoggingLevel;

public class ClusterTest extends SpellsourceTestBase {

	@Test(timeout = 155000L)
	public void testMultiHostMultiClientCluster(TestContext context) {
		// Connect to existing cluster
		Async latch = context.async(10);
		AtomicReference<Vertx> newVertx = new AtomicReference<>();
		HazelcastInstance instance = Hazelcast.newHazelcastInstance(Cluster.getConfig(5702));
		Vertx.clusteredVertx(new VertxOptions()
				.setClusterManager(new HazelcastClusterManager(instance))
				.setBlockedThreadCheckInterval(30000L)
				.setWarningExceptionTime(30000L), context.asyncAssertSuccess(v1 -> {
			// Deploy a second gateway
			newVertx.set(v1);
			v1.deployVerticle(Gateway.create(9090), context.asyncAssertSuccess(v2 -> {
				// Distribute clients to the two gateways
				Stream.generate(() -> Stream.of(8080, 9090)).flatMap(Function.identity())
						.map(port -> new Thread(() -> {
							UnityClient client = new UnityClient(context, port);
							client.createUserAccount();
							client.matchmakeAndPlay(null);
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
