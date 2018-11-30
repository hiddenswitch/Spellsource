package com.hiddenswitch.spellsource.concurrent;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.concurrent.impl.SuspendableAsyncMap;
import com.hiddenswitch.spellsource.concurrent.impl.SuspendableWrappedMap;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.vertx.ext.sync.Sync.awaitResult;

public interface SuspendableMap<K, V> {
	@Suspendable
	static <K, V> SuspendableMap<K, V> getOrCreate(String name) {
		System.setProperty("vertx.hazelcast.async-api", "true");
		final Vertx vertx = Vertx.currentContext().owner();
		io.vertx.core.shareddata.SharedData client = vertx.sharedData();
		if (vertx.isClustered()) {
			AsyncMap<K, V> map = awaitResult(done -> client.<K, V>getClusterWideMap(name, done));
			return new SuspendableAsyncMap<>(map);
		} else {
			return new SuspendableWrappedMap<>(client.<K, V>getLocalMap(name));
		}
	}

	static <K, V> void getOrCreate(String name, Handler<AsyncResult<AsyncMap<K, V>>> handler) {
		System.setProperty("vertx.hazelcast.async-api", "true");
		Vertx vertx = Vertx.currentContext().owner();
		io.vertx.core.shareddata.SharedData client = vertx.sharedData();
		client.getAsyncMap(name, handler);
	}

	@Suspendable
	int size();

	@Suspendable
	boolean isEmpty();

	@Suspendable
	@SuppressWarnings("unchecked")
	boolean containsKey(K key);

	@Suspendable
	boolean containsValue(V value);

	@Suspendable
	@SuppressWarnings("unchecked")
	V get(K key);

	@Suspendable
	V put(K key, V value);

	@Suspendable
	V putIfAbsent(K key, V value);

	@Suspendable
	@SuppressWarnings("unchecked")
	V remove(K key);

	@Suspendable
	void putAll(Map<? extends K, ? extends V> m);

	@Suspendable
	void clear();

	@Suspendable
	Set<K> keySet();

	@Suspendable
	Collection<V> values();

	@Suspendable
	Set<Map.Entry<K, V>> entrySet();

	@Suspendable
	default V replace(K key, V value) {
		V curValue;
		if (((curValue = get(key)) != null) || containsKey(key)) {
			curValue = put(key, value);
		}
		return curValue;
	}

	@Suspendable
	default boolean replace(K key, V oldValue, V newValue) {
		Object curValue = get(key);
		if (!Objects.equals(curValue, oldValue) ||
				(curValue == null && !containsKey(key))) {
			return false;
		}
		put(key, newValue);
		return true;
	}

	@Suspendable
	default boolean remove(K key, V value) {
		Object curValue = get(key);
		if (!Objects.equals(curValue, value) ||
				(curValue == null && !containsKey(key))) {
			return false;
		}
		remove(key);
		return true;
	}
}

