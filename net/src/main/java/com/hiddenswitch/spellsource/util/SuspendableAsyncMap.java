package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Future;
import io.vertx.core.shareddata.AsyncMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static io.vertx.ext.sync.Sync.awaitFiber;
import static io.vertx.ext.sync.Sync.awaitResult;

public final class SuspendableAsyncMap<K, V> implements SuspendableMap<K, V> {
	private final AsyncMap<K, V> map;

	SuspendableAsyncMap(AsyncMap<K, V> map) {
		this.map = map;
	}

	@Override
	@Suspendable
	public int size() {
		return awaitFiber(map::size);
	}

	@Override
	@Suspendable
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public boolean containsKey(Object key) {
		return awaitResult(done -> {
			map.get((K) key, then -> {
				done.handle(Future.succeededFuture(then.succeeded()));
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
		return awaitResult(h -> map.get((K) key, h));
	}

	@Override
	@Suspendable
	public V put(K key, V value) {
		return awaitResult(h -> map.put(key, value, then -> h.handle(Future.succeededFuture(value))));
	}

	@Override
	@Suspendable
	public V putIfAbsent(K key, V value) {
		return awaitResult(h -> map.putIfAbsent(key, value, h));
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		return awaitResult(h -> map.remove((K) key, h));
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public boolean remove(Object key, Object value) {
		return awaitResult(h -> map.removeIfPresent((K) key, (V) value, h));
	}

	@Override
	@Suspendable
	public boolean replace(K key, V oldValue, V newValue) {
		return awaitResult(h -> map.replaceIfPresent(key, oldValue, newValue, h));
	}

	@Override
	@Suspendable
	public V replace(K key, V value) {
		return awaitResult(h -> map.replace(key, value, h));
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
		awaitResult(map::clear);
	}

	@Override
	@Suspendable
	public Set<K> keySet() {
		return awaitResult(map::keys);
	}

	@Override
	@Suspendable
	public Collection<V> values() {
		return awaitResult(map::values);
	}

	@Override
	@Suspendable
	public Set<Map.Entry<K, V>> entrySet() {
		return awaitResult(map::entries).entrySet();
	}

	@Override
	public String toString() {
		return "SyncMap";
	}
}
