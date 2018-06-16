package com.hiddenswitch.spellsource.concurrent.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.hiddenswitch.spellsource.concurrent.SuspendableMultimap;
import io.reactivex.Observable;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocalMultimap<K, V> extends AbstractAddedChangedRemoved<K, V> implements SuspendableMultimap<K, V> {
	ArrayListMultimap<K, V> map = ArrayListMultimap.create();

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(@Nullable Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(@Nullable Object value) {
		return map.containsValue(value);
	}

	@Override
	public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
		return map.containsEntry(key, value);
	}

	@Override
	public boolean put(@Nullable K key, @Nullable V value) {
		boolean success = map.put(key, value);
		if (success) {
			added.onNext(Maps.immutableEntry(key, value));
		}
		return success;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(@Nullable Object key, @Nullable Object value) {
		boolean success = map.remove(key, value);
		if (success) {
			removed.onNext(Maps.immutableEntry((K) key, (V) value));
		}
		return success;
	}

	@Override
	public boolean putAll(@Nullable K key, Iterable<? extends V> values) {
		return map.putAll(key, values);
	}

	@Override
	public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
		return map.putAll(multimap);
	}

	@Override
	public List<V> replaceValues(@Nullable K key, Iterable<? extends V> values) {
		return map.replaceValues(key, values);
	}

	@Override
	public List<V> removeAll(@Nullable Object key) {
		return map.removeAll(key);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public List<V> get(@Nullable K key) {
		return map.get(key);
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Multiset<K> keys() {
		return map.keys();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}

	@Override
	public Collection<Map.Entry<K, V>> entries() {
		return map.entries();
	}

	@Override
	public Map<K, Collection<V>> asMap() {
		return map.asMap();
	}

	@Override
	public Observable<Map.Entry<K, V>> changed() {
		throw new UnsupportedOperationException("Multimaps are never updated.");
	}
}
