package com.hiddenswitch.spellsource.concurrent.impl;

import com.github.fromage.quasi.fibers.Suspendable;
import com.google.common.collect.Maps;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;
import com.hiddenswitch.spellsource.concurrent.impl.AbstractAddedChangedRemoved;
import com.hiddenswitch.spellsource.util.AddedChangedRemoved;

public final class RxEntryListenerAdaptor<K, V> extends AbstractAddedChangedRemoved<K, V> implements AddedChangedRemoved<K, V>, EntryListener<K, V> {

	public RxEntryListenerAdaptor() {
	}

	@Override
	@Suspendable
	public void entryAdded(EntryEvent<K, V> event) {
		added.onNext(Maps.immutableEntry(event.getKey(), event.getValue()));
	}

	@Override
	@Suspendable
	public void entryEvicted(EntryEvent<K, V> event) {
		removed.onNext(Maps.immutableEntry(event.getKey(), event.getValue()));
	}

	@Override
	@Suspendable
	public void entryRemoved(EntryEvent<K, V> event) {
		removed.onNext(Maps.immutableEntry(event.getKey(), event.getValue()));
	}

	@Override
	@Suspendable
	public void entryUpdated(EntryEvent<K, V> event) {
		changed.onNext(Maps.immutableEntry(event.getKey(), event.getValue()));
	}

	@Override
	@Suspendable
	public void mapCleared(MapEvent event) {
	}

	@Override
	@Suspendable
	public void mapEvicted(MapEvent event) {
	}
}
