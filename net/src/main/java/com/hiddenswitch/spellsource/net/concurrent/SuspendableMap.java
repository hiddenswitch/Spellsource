package com.hiddenswitch.spellsource.net.concurrent;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.concurrent.impl.SuspendableAsyncMap;
import com.hiddenswitch.spellsource.net.concurrent.impl.SuspendableWrappedMap;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static io.vertx.ext.sync.Sync.awaitResult;

public abstract class SuspendableMap<K, V> {
	private static Map<String, SuspendableMap> MAP_CACHE = new ConcurrentHashMap<>();

	@Suspendable
	public static <K, V> SuspendableMap<K, V> getOrCreate(String name) {
		Vertx vertx = Vertx.currentContext().owner();
		String key = vertx.hashCode() + name;

		@SuppressWarnings("unchecked")
		SuspendableMap<K, V> suspendableMap = MAP_CACHE.computeIfAbsent(key, new Function<String, SuspendableMap>() {
					@Override
					@Suspendable
					public SuspendableMap apply(String k) {
						SharedData client = vertx.sharedData();
						if (vertx.isClustered()) {
							AsyncMap<K, V> map = awaitResult(done -> client.getClusterWideMap(name, done));
							return new SuspendableAsyncMap<>(map);
						} else {
							return new SuspendableWrappedMap<>(client.getLocalMap(name));
						}
					}
				}
		);
		return suspendableMap;
	}

	public static <K, V> void getOrCreate(String name, Handler<AsyncResult<AsyncMap<K, V>>> handler) {
		Vertx vertx = Vertx.currentContext().owner();
		io.vertx.core.shareddata.SharedData client = vertx.sharedData();
		client.getAsyncMap(name, handler);
	}

	@Suspendable
	public abstract int size();

	@Suspendable
	public abstract boolean isEmpty();

	@Suspendable
	@SuppressWarnings("unchecked")
	public abstract boolean containsKey(K key);

	@Suspendable
	public abstract boolean containsValue(V value);

	@Suspendable
	@SuppressWarnings("unchecked")
	public abstract V get(K key);

	@Suspendable
	public abstract V put(K key, V value);

	@Suspendable
	public abstract V putIfAbsent(K key, V value);

	@Suspendable
	@SuppressWarnings("unchecked")
	public abstract V remove(K key);

	@Suspendable
	public abstract void putAll(Map<? extends K, ? extends V> m);

	@Suspendable
	public abstract void clear();

	@Suspendable
	public abstract Set<K> keySet();

	@Suspendable
	public abstract Collection<V> values();

	@Suspendable
	public abstract Set<Map.Entry<K, V>> entrySet();

	@Suspendable
	public V replace(K key, V value) {
		V curValue;
		if (((curValue = get(key)) != null) || containsKey(key)) {
			curValue = put(key, value);
		}
		return curValue;
	}

	@Suspendable
	public boolean replace(K key, V oldValue, V newValue) {
		Object curValue = get(key);
		if (!Objects.equals(curValue, oldValue) ||
				(curValue == null && !containsKey(key))) {
			return false;
		}
		put(key, newValue);
		return true;
	}

	@Suspendable
	public boolean remove(K key, V value) {
		Object curValue = get(key);
		if (!Objects.equals(curValue, value) ||
				(curValue == null && !containsKey(key))) {
			return false;
		}
		remove(key);
		return true;
	}
}

