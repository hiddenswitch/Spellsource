package com.hiddenswitch.spellsource.net.concurrent.impl;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import com.google.common.collect.*;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableMultimap;
import com.hiddenswitch.spellsource.net.impl.Sync;
import io.atomix.core.iterator.AsyncIterator;
import io.atomix.core.multimap.AsyncAtomicMultimap;
import io.atomix.core.multimap.AtomicMultimapEvent;
import io.atomix.core.multimap.AtomicMultimapEventListener;
import io.atomix.vertx.AtomixAsyncMultiMap;
import io.reactivex.Observable;
import io.reactivex.subjects.Subject;
import io.reactivex.subjects.UnicastSubject;
import io.vertx.core.Vertx;
import io.vertx.reactivex.RxHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static io.vertx.ext.sync.Sync.awaitResult;

public class SuspendableAtomixMultimap<K, V> implements SuspendableMultimap<K, V> {
	private final Vertx vertx;
	private String name;
	private AtomixAsyncMultiMap<K, V> map = null;
	private ReentrantLock mapLock = new ReentrantLock();

	public SuspendableAtomixMultimap(String name, Vertx vertx) {
		this.name = name;
		this.vertx = vertx;
	}

	@Suspendable
	protected AsyncAtomicMultimap<K, V> getOrCreate() {
		mapLock.lock();
		try {
			if (map != null) {
				map = awaitResult(h -> AtomixHelpers.getClusterManager(vertx).getAsyncMultiMap(name, v -> h.handle(v.map(innerMap -> {
					@SuppressWarnings("unchecked")
					AtomixAsyncMultiMap<K, V> innerMap1 = (AtomixAsyncMultiMap<K, V>) innerMap;
					return innerMap1;
				}))));
			}
		} finally {
			mapLock.unlock();
		}
		return map.getMap();
	}

	@Override
	@Suspendable
	public int size() {
		return Sync.get(getOrCreate().size());
	}

	@Override
	@Suspendable
	public boolean isEmpty() {
		return Sync.get(getOrCreate().isEmpty());
	}

	@Override
	@Suspendable
	public boolean containsKey(@Nullable K key) {
		return Sync.get(getOrCreate().containsKey(key));
	}

	@Override
	@Suspendable
	public boolean containsValue(@Nullable V value) {
		return Sync.get(getOrCreate().containsValue(value));
	}

	@Override
	@Suspendable
	public boolean containsEntry(@Nullable K key, @Nullable V value) {
		return Sync.get(getOrCreate().containsEntry(key, value));
	}

	@Override
	@Suspendable
	public boolean put(@Nullable K key, @Nullable V value) {
		return Sync.get(getOrCreate().put(key, value));
	}

	@Override
	@Suspendable
	public boolean remove(@Nullable K key, @Nullable V value) {
		return Sync.get(getOrCreate().remove(key, value));
	}

	@Override
	@Suspendable
	public boolean putAll(@Nullable K key, Iterable<? extends V> values) {
		return Sync.get(getOrCreate().putAll(key, Lists.newArrayList(values)));
	}

	@Override
	@Suspendable
	public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Suspendable
	public List<V> replaceValues(@Nullable K key, Iterable<? extends V> values) {
		return new ArrayList<>(Sync.get(getOrCreate().replaceValues(key, Lists.newArrayList(values))).value());
	}

	@Override
	@Suspendable
	public List<V> removeAll(@Nullable K key) {
		return new ArrayList<>(Sync.get(getOrCreate().removeAll(key)).value());
	}

	@Override
	@Suspendable
	public void clear() {
		Sync.get(getOrCreate().clear());
	}

	@Override
	@Suspendable
	public List<V> get(@Nullable K key) {
		return new ArrayList<>(Sync.get(getOrCreate().get(key)).value());
	}

	@Suspendable
	protected <T extends Collection<S>, S> T iterate(Supplier<T> constructor, AsyncIterator<S> iterator) {
		try {
			T set = constructor.get();
			while (Sync.get(iterator.hasNext())) {
				set.add(Sync.get(iterator.next()));
			}
			return set;
		} finally {
			iterator.close();
		}
	}

	@Override
	@Suspendable
	public Set<K> keySet() {
		return iterate(HashSet::new, getOrCreate().keySet().iterator());
	}

	@Override
	@Suspendable
	public Multiset<K> keys() {
		return iterate(HashMultiset::create, getOrCreate().keySet().iterator());
	}

	@Override
	@Suspendable
	public Collection<V> values() {
		return iterate(Lists::newArrayList, getOrCreate().values().iterator());
	}

	@Override
	@Suspendable
	public Collection<Map.Entry<K, V>> entries() {
		return iterate(Lists::newArrayList, getOrCreate().entries().iterator());
	}

	@Override
	@Suspendable
	public Map<K, Collection<V>> asMap() {
		throw new UnsupportedOperationException();
	}

	@Override
	@Suspendable
	public Observable<Map.Entry<K, V>> added() {
		return getEntryObservable(AtomicMultimapEvent.Type.INSERT);
	}

	@Suspendable
	protected Observable<Map.Entry<K, V>> getEntryObservable(AtomicMultimapEvent.Type type) {
		Subject<AtomicMultimapEvent<K, V>> subject = UnicastSubject.create();
		AtomicMultimapEventListener<K, V> kvAtomicMultimapEventListener = subject::onNext;
		Void v1 = Sync.get(getOrCreate().addListener(kvAtomicMultimapEventListener));

		return subject
				.observeOn(RxHelper.scheduler(Vertx.currentContext()))
				.subscribeOn(RxHelper.scheduler(Vertx.currentContext()))
				.doOnDispose(() -> {
					if (Vertx.currentContext() == null) {
						if (Fiber.isCurrentFiber()) {
							Void v2 = Sync.get(getOrCreate().removeListener(kvAtomicMultimapEventListener));
						} else {
							AtomixHelpers.getClusterManager().getAsyncMultiMap(name, v -> v.map(innerMap -> {
								@SuppressWarnings("unchecked")
								AtomixAsyncMultiMap<K, V> innerMap1 = (AtomixAsyncMultiMap<K, V>) innerMap;
								return innerMap1;
							}).result().getMap().removeListener(kvAtomicMultimapEventListener));
						}
					} else {
						map.getMap().removeListener(kvAtomicMultimapEventListener);
					}
				})
				.filter(event -> {
					return event.type() == type;
				})
				.map(event -> Maps.immutableEntry(event.key(), event.newValue()));
	}

	@Override
	@Suspendable
	public Observable<Map.Entry<K, V>> changed() {
		throw new UnsupportedOperationException();
	}

	@Override
	@Suspendable
	public Observable<Map.Entry<K, V>> removed() {
		return getEntryObservable(AtomicMultimapEvent.Type.REMOVE);
	}
}
