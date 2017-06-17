package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Future;
import io.vertx.core.shareddata.AsyncMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static io.vertx.ext.sync.Sync.awaitResult;

public class SuspendableMap<K, V> implements Map<K, V> {
	private final AsyncMap<K, V> map;

	SuspendableMap(AsyncMap<K, V> map) {
		this.map = map;
	}

	@Override
	@Suspendable
	public int size() {
		return awaitResult(map::size);
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
		return awaitResult(h -> map.get((K)key, h));
	}

	@Override
	@Suspendable
	public V put(K key, V value) {
		return awaitResult(h -> map.put(key, value, then -> h.handle(Future.succeededFuture(value))));
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		return awaitResult(h -> map.remove((K)key, h));
	}

	@Override
	@Suspendable
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	@Suspendable
	public void clear() {
		awaitResult(map::clear);
	}

	@Override
	public Set<K> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}
}
