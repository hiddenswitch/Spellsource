package com.hiddenswitch.spellsource.concurrent.impl;

import com.hiddenswitch.spellsource.util.AddedChangedRemoved;
import io.reactivex.Observable;

import java.util.Map;

public class SingleKeyAddedChangedRemoved<K, V> implements AddedChangedRemoved<K, V> {
	private final K key;
	private final AddedChangedRemoved<K, V> map;

	public SingleKeyAddedChangedRemoved(K key, AddedChangedRemoved<K, V> map) {
		this.key = key;
		this.map = map;
	}

	@Override
	public Observable<Map.Entry<K, V>> added() {
		return map.added().filter(kv -> kv.getKey().equals(key));
	}

	@Override
	public Observable<Map.Entry<K, V>> changed() {
		return map.changed().filter(kv -> kv.getKey().equals(key));
	}

	@Override
	public Observable<Map.Entry<K, V>> removed() {
		return map.removed().filter(kv -> kv.getKey().equals(key));
	}
}
