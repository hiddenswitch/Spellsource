package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Lock;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.vertx.ext.sync.Sync.awaitResult;

public class SharedData {
	private static Map<String, LocalMultimap> localMultimaps = new ConcurrentHashMap<>();

	@Suspendable
	public static <K, V> SuspendableMap<K, V> getClusterWideMap(String name) throws SuspendExecution {
		return getClusterWideMap(name, Vertx.currentContext().owner());
	}

	@Suspendable
	public static <K, V> SuspendableMap<K, V> getClusterWideMap(String name, final Vertx vertx) throws SuspendExecution {
		io.vertx.core.shareddata.SharedData client = vertx.sharedData();
		if (vertx.isClustered()) {
			AsyncMap<K, V> map = awaitResult(done -> client.<K, V>getClusterWideMap(name, done));
			return new SuspendableAsyncMap<>(map);
		} else {
			return new SuspendableWrappedMap<>(client.<K, V>getLocalMap(name));
		}
	}

	@Suspendable
	@SuppressWarnings("unchecked")
	public static <K, V> SuspendableMultimap<K, V> getClusterWideMultimap(String name) throws SuspendExecution {
		final Vertx vertx = Vertx.currentContext().owner();
		if (vertx.isClustered()) {
			return new SuspendableHazelcastMultimap<>(name);
		} else {
			return (SuspendableMultimap<K, V>) localMultimaps.computeIfAbsent(name, (k) -> new LocalMultimap<K, V>());
		}
	}

	@Suspendable
	public static <K, V> AddedChangedRemoved<K, V> subscribeToKeyInMultimap(String name, K key) throws SuspendExecution {
		final Vertx vertx = Vertx.currentContext().owner();
		if (vertx.isClustered()) {
			final RxEntryListenerAdaptor<K, V> adaptor = new RxEntryListenerAdaptor<>();
			MultiMap<K, V> map = Sync.invoke(getHazelcastInstance()::getMultiMap, name);
			map.addEntryListener(adaptor, key, true);
			return adaptor;
		} else {
			SuspendableMultimap<K, V> map = getClusterWideMultimap(name);
			return new SingleKeyAddedChangedRemoved<>(key, map);
		}
	}

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
}
