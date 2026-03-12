package net.demilich.metastone.game.cards;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A base map wrapping extending a {@link HashMap} or {@link EnumMap} depending on the memory needs of this server
 * instance.
 *
 * @param <K>
 * @param <V>
 */
public abstract class BaseMap<K extends Enum<K>, V> extends AbstractMap<K, V> implements Freezable, Cloneable, Serializable {
	private transient boolean readOnly;
	private final Class<K> keyType;
	private final K[] keyUniverse;
	private SharedData<V> data;
	private transient Set<Entry<K, V>> entrySet;

	public BaseMap(Class<K> keyType) {
		this.keyType = keyType;
		this.keyUniverse = keyType.getEnumConstants();
		this.data = new SharedData<>(keyUniverse.length);
	}

	BaseMap(Class<K> keyType, Map<K, V> m) {
		this(keyType);
		if (m != null && !m.isEmpty()) {
			putAll(m);
		}
	}

	@Override
	public String toString() {
		return super.toString();
	}

	/* TODO: To switch to a different implementation, re-enable this
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		V val = get(key);
		if (val == null) {
			return defaultValue;
		}
		return val;
	}
	*/

	protected void checkNotFrozen() {
		if (readOnly) {
			throw new UnsupportedOperationException("This object is frozen (read-only) and cannot be modified.");
		}
	}

	@Override
	public V put(K key, V value) {
		checkNotFrozen();
		Objects.requireNonNull(key);
		ensureOwned();
		return data.put(key.ordinal(), value);
	}

	@Override
	public V remove(Object key) {
		checkNotFrozen();
		int ordinal = ordinalOf(key);
		if (ordinal < 0 || !data.present[ordinal]) {
			return null;
		}
		ensureOwned();
		return data.remove(ordinal);
	}

	@Override
	public void clear() {
		checkNotFrozen();
		if (data.size == 0) {
			return;
		}
		ensureOwned();
		data.clear();
	}

	@Override
	public V get(Object key) {
		int ordinal = ordinalOf(key);
		if (ordinal < 0 || !data.present[ordinal]) {
			return null;
		}
		return data.get(ordinal);
	}

	@Override
	public boolean containsKey(Object key) {
		int ordinal = ordinalOf(key);
		return ordinal >= 0 && data.present[ordinal];
	}

	@Override
	public int size() {
		return data.size;
	}

	@Override
	public boolean isEmpty() {
		return data.size == 0;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		checkNotFrozen();
		if (m == null || m.isEmpty()) {
			return;
		}
		ensureOwned();
		for (var entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		if (entrySet == null) {
			entrySet = new EntrySet();
		}
		return entrySet;
	}

	@Override
	public Collection<V> values() {
		return super.values();
	}

	@Override
	public Set<K> keySet() {
		return super.keySet();
	}

	@Override
	public void freeze() {
		readOnly = true;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public BaseMap<K, V> clone() {
		try {
			BaseMap<K, V> clone = (BaseMap<K, V>) super.clone();
			clone.data = data.retain();
			clone.readOnly = this.readOnly;
			clone.entrySet = null;
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Creates a deep copy with readOnly set to false.
	 */
	public BaseMap<K, V> cloneAndUnfreeze() {
		BaseMap<K, V> clone = clone();
		clone.readOnly = false;
		return clone;
	}

	private void ensureOwned() {
		if (data.isShared()) {
			data.release();
			data = data.copy();
		}
	}

	private int ordinalOf(Object key) {
		if (!keyType.isInstance(key)) {
			return -1;
		}
		return ((Enum<?>) key).ordinal();
	}

	private final class EntrySet extends AbstractSet<Entry<K, V>> {
		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new Iterator<>() {
				private int nextIndex = findNext(0);
				private int currentIndex = -1;

				@Override
				public boolean hasNext() {
					return nextIndex >= 0;
				}

				@Override
				public Entry<K, V> next() {
					if (nextIndex < 0) {
						throw new NoSuchElementException();
					}
					currentIndex = nextIndex;
					nextIndex = findNext(currentIndex + 1);
					return new BaseMapEntry(currentIndex);
				}

				@Override
				public void remove() {
					if (currentIndex < 0) {
						throw new IllegalStateException();
					}
					BaseMap.this.remove(keyUniverse[currentIndex]);
					currentIndex = -1;
				}
			};
		}

		@Override
		public int size() {
			return BaseMap.this.size();
		}

		@Override
		public void clear() {
			BaseMap.this.clear();
		}
	}

	private final class BaseMapEntry implements Entry<K, V> {
		private final int ordinal;

		private BaseMapEntry(int ordinal) {
			this.ordinal = ordinal;
		}

		@Override
		public K getKey() {
			return keyUniverse[ordinal];
		}

		@Override
		public V getValue() {
			return data.get(ordinal);
		}

		@Override
		public V setValue(V value) {
			return BaseMap.this.put(getKey(), value);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Entry<?, ?> other)) {
				return false;
			}
			return Objects.equals(getKey(), other.getKey()) && Objects.equals(getValue(), other.getValue());
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
		}
	}

	private int findNext(int fromIndex) {
		for (int i = fromIndex; i < data.present.length; i++) {
			if (data.present[i]) {
				return i;
			}
		}
		return -1;
	}

	private static final class SharedData<V> implements Serializable {
		private final Object[] values;
		private final boolean[] present;
		private final AtomicInteger references;
		private int size;

		private SharedData(int capacity) {
			this(new Object[capacity], new boolean[capacity], 0);
		}

		private SharedData(Object[] values, boolean[] present, int size) {
			this.values = values;
			this.present = present;
			this.size = size;
			this.references = new AtomicInteger(1);
		}

		private SharedData<V> retain() {
			references.incrementAndGet();
			return this;
		}

		private void release() {
			references.decrementAndGet();
		}

		private boolean isShared() {
			return references.get() > 1;
		}

		private SharedData<V> copy() {
			return new SharedData<>(values.clone(), present.clone(), size);
		}

		@SuppressWarnings("unchecked")
		private V get(int ordinal) {
			return (V) values[ordinal];
		}

		@SuppressWarnings("unchecked")
		private V put(int ordinal, V value) {
			V previous = (V) values[ordinal];
			values[ordinal] = value;
			if (!present[ordinal]) {
				present[ordinal] = true;
				size++;
			}
			return previous;
		}

		@SuppressWarnings("unchecked")
		private V remove(int ordinal) {
			if (!present[ordinal]) {
				return null;
			}
			V previous = (V) values[ordinal];
			values[ordinal] = null;
			present[ordinal] = false;
			size--;
			return previous;
		}

		private void clear() {
			Arrays.fill(values, null);
			Arrays.fill(present, false);
			size = 0;
		}
	}
}
