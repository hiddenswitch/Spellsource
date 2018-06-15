package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Lock;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static io.vertx.ext.sync.Sync.awaitResult;

public class SharedData {

	public static HazelcastInstance getHazelcastInstance() {
		return getClusterManager().getHazelcastInstance();
	}

	public static HazelcastClusterManager getClusterManager() {
		return (HazelcastClusterManager) ((VertxInternal) (Vertx.currentContext().owner())).getClusterManager();
	}

	@Suspendable
	public static Lock lock(String name, long timeout) {
		final Vertx vertx = Vertx.currentContext().owner();
		return awaitResult(h -> vertx.sharedData().getLockWithTimeout(name, timeout, h));
	}

	@Suspendable
	public static Lock lock(String name) {
		return invoke(Vertx.currentContext().owner().sharedData()::getLock, name);
	}
}
