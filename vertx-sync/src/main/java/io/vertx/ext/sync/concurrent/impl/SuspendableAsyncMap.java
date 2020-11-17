package io.vertx.ext.sync.concurrent.impl;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.ext.sync.concurrent.SuspendableMap;
import io.vertx.core.Future;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.sync.Sync;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.vertx.ext.sync.Sync.await;

public final class SuspendableAsyncMap<K, V> extends SuspendableMap<K, V> {
	private final String name;
	private final AsyncMap<K, V> map;

	public SuspendableAsyncMap(String name, AsyncMap<K, V> map) {
		this.name = name;
		this.map = map;
	}

	@Override
	@Suspendable
	public int size() {
		return Sync.await(map::size);
	}

	@Override
	@Suspendable
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	@Suspendable
	public boolean containsKey(K key) {
		if (key == null) {
			throw new NullPointerException("key");
		}

		return Sync.await(done -> {
			map.get(key, then -> {
				done.handle(Future.succeededFuture(then.succeeded() && then.result() != null));
			});
		});
	}

	@Override
	@Suspendable
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("Cannot check if a value is contained by this map.");
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public V get(Object key) {
		return Sync.await(h -> map.get((K) key, h));
	}

	@Override
	@Suspendable
	public V put(K key, V value) {
		return Sync.await(h -> map.put(key, value, then -> h.handle(then.map(value))));
	}

	@Override
	@Suspendable
	public V put(K key, V value, long timeToLiveMillis) {
		return Sync.await(h -> map.put(key, value, timeToLiveMillis, then -> h.handle(then.map(value))));
	}

	@Override
	@Suspendable
	public V putIfAbsent(K key, V value) {
		return Sync.await(h -> map.putIfAbsent(key, value, h));
	}

	@Override
	@Suspendable
	public V putIfAbsent(K key, V value, long timeToLiveMillis) {
		return Sync.await(h -> map.putIfAbsent(key, value, timeToLiveMillis, h));
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public V remove(K key) {
		return Sync.await(h -> map.remove(key, h));
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public boolean remove(K key, V value) {
		Boolean res = Sync.await(h -> map.removeIfPresent((K) key, (V) value, h));
		return res;
	}

	@Override
	@Suspendable
	public boolean replace(K key, V oldValue, V newValue) {
		return Sync.await(h -> map.replaceIfPresent(key, oldValue, newValue, h));
	}

	@Override
	@Suspendable
	public V replace(K key, V value) {
		return Sync.await(h -> map.replace(key, value, h));
	}

	@Override
	@Suspendable
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	@Suspendable
	public void clear() {
		Void t = Sync.await(map::clear);
	}

	@Override
	@Suspendable
	public Set<K> keySet() {
		return Sync.await(map::keys);
	}

	@Override
	@Suspendable
	public List<V> values() {
		return Sync.await(map::values);
	}

	@Override
	@Suspendable
	public Set<Map.Entry<K, V>> entrySet() {
		Map<K,V> v = Sync.await(map::entries);
		return v.entrySet();
	}

	@Override
	public String toString() {
		return "SyncMap";
	}
}
