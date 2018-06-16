package com.hiddenswitch.spellsource.concurrent.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.hazelcast.core.MultiMap;
import com.hiddenswitch.spellsource.util.Hazelcast;
import com.hiddenswitch.spellsource.concurrent.SuspendableMultimap;
import io.reactivex.Observable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static com.hiddenswitch.spellsource.util.Sync.invoke0;

public class SuspendableHazelcastMultimap<K, V> implements SuspendableMultimap<K, V> {
	private final MultiMap<K, V> map;
	private final RxEntryListenerAdaptor<K, V> adaptor;

	public SuspendableHazelcastMultimap(String name) {
		map = Hazelcast.getHazelcastInstance().getMultiMap(name);
		adaptor = new RxEntryListenerAdaptor<>();
		map.addEntryListener(adaptor, true);
	}

	@Override
	@Suspendable
	public int size() {
		return map.size();
	}

	@Override
	@Suspendable
	public boolean isEmpty() {
		return map.size() == 0;
	}

	@Override
	@Suspendable
	public boolean containsKey(@Nullable K key) {
		return map.containsKey(key);
	}

	@Override
	@Suspendable
	public boolean containsValue(@Nullable V value) {
		return invoke(map::containsValue, value);
	}

	@Override
	@Suspendable
	public boolean containsEntry(@Nullable K key, @Nullable V value) {
		return invoke(map::containsEntry, key, value);
	}

	@Override
	@Suspendable
	public boolean put(@Nullable K key, @Nullable V value) {
		return invoke(map::put, key, value);
	}

	@Override
	@Suspendable
	public boolean remove(@Nullable Object key, @Nullable Object value) {
		return invoke(map::remove, key, value);
	}

	@Override
	@Suspendable
	public boolean putAll(@Nullable K key, Iterable<? extends V> values) {
		for (V val : values) {
			if (!invoke(map::put, key, val)) {
				return false;
			}
		}
		return true;
	}

	@Override
	@Suspendable
	public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
		throw new UnsupportedOperationException("putAll");
	}

	@Override
	@Suspendable
	public List<V> replaceValues(@Nullable K key, Iterable<? extends V> values) {
		throw new UnsupportedOperationException("replaceValues");
	}

	@Override
	@Suspendable
	public List<V> removeAll(@Nullable K key) {
		List<V> invoke = new ArrayList<>(invoke((Function<Object, Collection<V>>) map::remove, key));
		return new ArrayList<>(invoke);
	}

	@Override
	@Suspendable
	public void clear() {
		invoke0(map::clear);
	}

	@Override
	@Suspendable
	public List<V> get(@Nullable K key) {
		return new ArrayList<>(invoke(map::get, key));
	}

	@Override
	@Suspendable
	public Set<K> keySet() {
		return invoke(map::keySet);
	}

	@Override
	@Suspendable
	public Multiset<K> keys() {
		throw new UnsupportedOperationException("keys");
	}

	@Override
	@Suspendable
	public Collection<V> values() {
		return invoke(map::values);
	}

	@Override
	@Suspendable
	public Collection<Map.Entry<K, V>> entries() {
		return invoke(map::entrySet);
	}

	@Override
	@Suspendable
	public Map<K, Collection<V>> asMap() {
		throw new UnsupportedOperationException("asMap");
	}

	@Override
	public Observable<Map.Entry<K, V>> added() {
		return adaptor.added();
	}

	@Override
	public Observable<Map.Entry<K, V>> changed() {
		throw new UnsupportedOperationException("Multimaps are never updated.");
	}

	@Override
	public Observable<Map.Entry<K, V>> removed() {
		return adaptor.removed();
	}
}
