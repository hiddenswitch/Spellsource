package com.hiddenswitch.spellsource.util;

import io.vertx.core.shareddata.LocalMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SuspendableWrappedMap<K, V> implements SuspendableMap<K, V> {
	private final LocalMap<K, V> localMap;

	public SuspendableWrappedMap(LocalMap<K, V> localMap) {
		this.localMap = localMap;
	}

	@Override
	public int size() {
		return localMap.size();
	}

	@Override
	public boolean isEmpty() {
		return localMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return localMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return localMap.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return localMap.get(key);
	}

	@Override
	public V put(K key, V value) {
		return localMap.put(key, value);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return localMap.putIfAbsent(key, value);
	}

	@Override
	public V remove(Object key) {
		return localMap.remove(key);
	}

	@Override
	public boolean remove(Object key, Object value) {
		return localMap.remove(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		localMap.putAll(m);
	}

	@Override
	public void clear() {
		localMap.clear();
	}

	@Override
	public Set<K> keySet() {
		return localMap.keySet();
	}

	@Override
	public Collection<V> values() {
		return localMap.values();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return localMap.entrySet();
	}
}
