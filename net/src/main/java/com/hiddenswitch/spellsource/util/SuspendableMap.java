package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.shareddata.LocalMap;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface SuspendableMap<K, V> {
	@Suspendable
	int size();

	@Suspendable
	boolean isEmpty();

	@Suspendable
	@SuppressWarnings("unchecked")
	boolean containsKey(Object key);

	@Suspendable
	boolean containsValue(Object value);

	@Suspendable
	@SuppressWarnings("unchecked")
	V get(Object key);

	@Suspendable
	V put(K key, V value);

	@Suspendable
	V putIfAbsent(K key, V value);

	@Suspendable
	@SuppressWarnings("unchecked")
	V remove(Object key);

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
	default boolean remove(Object key, Object value) {
		Object curValue = get(key);
		if (!Objects.equals(curValue, value) ||
				(curValue == null && !containsKey(key))) {
			return false;
		}
		remove(key);
		return true;
	}
}
