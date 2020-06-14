package com.hiddenswitch.spellsource.net.concurrent;


import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.hiddenswitch.spellsource.net.concurrent.impl.LocalMultimap;
import com.hiddenswitch.spellsource.net.concurrent.impl.SingleKeyAddedChangedRemoved;
import com.hiddenswitch.spellsource.net.concurrent.impl.SuspendableAtomixMultimap;
import com.hiddenswitch.spellsource.net.impl.AddedChangedRemoved;
import io.vertx.core.Vertx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public interface SuspendableMultimap<K, V> extends AddedChangedRemoved<K, V> {
	Map<String, SuspendableMultimap> MAP_CACHE = new HashMap<>();
	ReentrantLock LOCK = new ReentrantLock();

	@Suspendable
	static <K, V> SuspendableMultimap<K, V> create(Vertx vertx, String name) {
		if (vertx.isClustered()) {
			return new SuspendableAtomixMultimap<>(name, vertx);
		} else {
			return new LocalMultimap<K, V>();
		}
	}

	@Suspendable
	@SuppressWarnings("unchecked")
	static <K, V> SuspendableMultimap<K, V> getOrCreate(String name) {
		Vertx vertx = Vertx.currentContext().owner();
		String key = vertx.hashCode() + name;

		LOCK.lock();
		try {
			SuspendableMultimap<K, V> v;
			if ((v = MAP_CACHE.get(key)) == null) {
				SuspendableMultimap<K, V> newValue = create(vertx, key);
				MAP_CACHE.put(key, newValue);
				return newValue;
			}
			return v;
		} finally {
			LOCK.unlock();
		}
	}

	@Suspendable
	static <K, V> AddedChangedRemoved<K, V> subscribeToKeyInMultimap(String name, K key) throws SuspendExecution {
		SuspendableMultimap<K, V> map = getOrCreate(name);
		return new SingleKeyAddedChangedRemoved<>(key, map);
	}

	// Query Operations

	/**
	 * Returns the number of key-value pairs in this multimap.
	 * <p>
	 * <p><b>Note:</b> this method does not return the number of <i>distinct keys</i> in the multimap, which is given by
	 * {@code keySet().size()} or {@code asMap().size()}. See the opening section of the {@link Multimap} class
	 * documentation for clarification.
	 */
	@Suspendable
	int size();

	/**
	 * Returns {@code true} if this multimap contains no key-value pairs. Equivalent to {@code size() == 0}, but can in
	 * some cases be more efficient.
	 */
	@Suspendable
	boolean isEmpty();

	/**
	 * Returns {@code true} if this multimap contains at least one key-value pair with the key {@code key}.
	 */
	@Suspendable
	boolean containsKey(@Nullable K key);

	/**
	 * Returns {@code true} if this multimap contains at least one key-value pair with the value {@code value}.
	 */
	@Suspendable
	boolean containsValue(@Nullable V value);

	/**
	 * Returns {@code true} if this multimap contains at least one key-value pair with the key {@code key} and the value
	 * {@code value}.
	 */
	@Suspendable
	boolean containsEntry(@Nullable K key, @Nullable V value);

	// Modification Operations

	/**
	 * Stores a key-value pair in this multimap.
	 * <p>
	 * <p>Some multimap implementations allow duplicate key-value pairs, in which case {@code put} always adds a new
	 * key-value pair and increases the multimap size by 1. Other implementations prohibit duplicates, and storing a
	 * key-value pair that's already in the multimap has no effect.
	 *
	 * @return {@code true} if the method increased the size of the multimap, or {@code false} if the multimap already
	 * contained the key-value pair and doesn't allow duplicates
	 */
	@Suspendable
	boolean put(@Nullable K key, @Nullable V value);

	/**
	 * Removes a single key-value pair with the key {@code key} and the value {@code value} from this multimap, if such
	 * exists. If multiple key-value pairs in the multimap fit this description, which one is removed is unspecified.
	 *
	 * @return {@code true} if the multimap changed
	 */
	@Suspendable
	boolean remove(@Nullable K key, @Nullable V value);

	// Bulk Operations

	/**
	 * Stores a key-value pair in this multimap for each of {@code values}, all using the same key, {@code key}.
	 * Equivalent to (but expected to be more
	 * efficient than): <pre>   {@code
	 * <p>
	 *   for (V value : values) {
	 *     put(key, value);
	 *   }}</pre>
	 * <p>
	 * <p>In particular, this is a no-op if {@code values} is empty.
	 *
	 * @return {@code true} if the multimap changed
	 */
	@Suspendable
	boolean putAll(@Nullable K key, Iterable<? extends V> values);

	/**
	 * Stores all key-value pairs of {@code multimap} in this multimap, in the order returned by {@code
	 * multimap.entries()}.
	 *
	 * @return {@code true} if the multimap changed
	 */
	@Suspendable
	boolean putAll(Multimap<? extends K, ? extends V> multimap);

	/**
	 * Stores a collection of values with the same key, replacing any existing values for that key.
	 * <p>
	 * <p>If {@code values} is empty, this is equivalent to {@link #removeAll(Object) removeAll(key)}.
	 *
	 * @return the collection of replaced values, or an empty collection if no values were previously associated with the
	 * key. The collection <i>may</i> be modifiable, but updating it will have no effect on the multimap.
	 */
	@Suspendable
	List<V> replaceValues(@Nullable K key, Iterable<? extends V> values);

	/**
	 * Removes all values associated with the key {@code key}.
	 * <p>
	 * <p>Once this method returns, {@code key} will not be mapped to any values, so it will not appear in {@link
	 * #keySet()}, {@link #asMap()}, or any other views.
	 *
	 * @return the values that were removed (possibly empty). The returned collection <i>may</i> be modifiable, but
	 * updating it will have no effect on the multimap.
	 */
	@Suspendable
	List<V> removeAll(@Nullable K key);

	/**
	 * Removes all key-value pairs from the multimap, leaving it {@linkplain #isEmpty empty}.
	 */
	@Suspendable
	void clear();

	// Views

	/**
	 * Returns a view collection of the values associated with {@code key} in this multimap, if any. Note that when {@code
	 * containsKey(key)} is false, this returns an empty collection, not {@code null}.
	 * <p>
	 * <p>Changes to the returned collection will update the underlying multimap, and vice versa.
	 */
	@Suspendable
	List<V> get(@Nullable K key);

	/**
	 * Returns a view collection of all <i>distinct</i> keys contained in this multimap. Note that the key set contains a
	 * key if and only if this multimap maps that key to at least one value.
	 * <p>
	 * <p>Changes to the returned set will update the underlying multimap, and vice versa. However, <i>adding</i> to the
	 * returned set is not possible.
	 */
	@Suspendable
	Set<K> keySet();

	/**
	 * Returns a view collection containing the key from each key-value pair in this multimap, <i>without</i> collapsing
	 * duplicates. This collection has the same size as this multimap, and {@code keys().count(k) == get(k).size()} for
	 * all {@code k}.
	 * <p>
	 * <p>Changes to the returned multiset will update the underlying multimap, and vice versa. However, <i>adding</i>
	 * to the returned collection is not possible.
	 */
	@Suspendable
	Multiset<K> keys();

	/**
	 * Returns a view collection containing the <i>value</i> from each key-value pair contained in this multimap, without
	 * collapsing duplicates (so {@code values().size() == size()}).
	 * <p>
	 * <p>Changes to the returned collection will update the underlying multimap, and vice versa. However, <i>adding</i>
	 * to the returned collection is not possible.
	 */
	@Suspendable
	Collection<V> values();

	/**
	 * Returns a view collection of all key-value pairs contained in this multimap, as {@link Map.Entry} instances.
	 * <p>
	 * <p>Changes to the returned collection or the entries it contains will update the underlying multimap, and vice
	 * versa. However, <i>adding</i> to the returned collection is not possible.
	 */
	@Suspendable
	Collection<Map.Entry<K, V>> entries();

	/**
	 * Returns a view of this multimap as a {@code Map} from each distinct key to the nonempty collection of that key's
	 * associated values. Note that {@code this.asMap().get(k)} is equivalent to {@code this.get(k)} only when {@code k}
	 * is a key contained in the multimap; otherwise it returns {@code null} as opposed to an empty collection.
	 * <p>
	 * <p>Changes to the returned map or the collections that serve as its values will update the underlying multimap,
	 * and vice versa. The map does not support {@code put} or {@code putAll}, nor do its entries support {@link
	 * Map.Entry#setValue setValue}.
	 */
	@Suspendable
	Map<K, Collection<V>> asMap();

	// Comparison and hashing

	/**
	 * Compares the specified object with this multimap for equality. Two multimaps are equal when their map views, as
	 * returned by {@link #asMap}, are also equal.
	 * <p>
	 * <p>In general, two multimaps with identical key-value mappings may or may not be equal, depending on the
	 * implementation. For example, two {@link SetMultimap} instances with the same key-value mappings are equal, but
	 * equality of two {@link ListMultimap} instances depends on the ordering of the values for each key.
	 * <p>
	 * <p>A non-empty {@link SetMultimap} cannot be equal to a non-empty {@link ListMultimap}, since their {@link
	 * #asMap} views contain unequal collections as values. However, any two empty multimaps are equal, because they both
	 * have empty {@link #asMap} views.
	 */
	@Override
	@Suspendable
	boolean equals(@NotNull Object obj);

	/**
	 * Returns the hash code for this multimap.
	 * <p>
	 * <p>The hash code of a multimap is defined as the hash code of the map view, as returned by {@link
	 * Multimap#asMap}.
	 * <p>
	 * <p>In general, two multimaps with identical key-value mappings may or may not have the same hash codes, depending
	 * on the implementation. For example, two {@link SetMultimap} instances with the same key-value mappings will have
	 * the same {@code hashCode}, but the {@code hashCode} of {@link ListMultimap} instances depends on the ordering of
	 * the values for each key.
	 */
	@Override
	@Suspendable
	int hashCode();
}
