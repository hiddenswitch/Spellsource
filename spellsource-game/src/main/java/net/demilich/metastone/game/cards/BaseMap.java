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

/**
 * A base map wrapping extending a {@link HashMap} or {@link EnumMap} depending on the memory needs of this server
 * instance.
 *
 * @param <K>
 * @param <V>
 */
public abstract class BaseMap<K extends Enum<K>, V> extends AbstractMap<K, V> implements Freezable, Cloneable, Serializable {
	private static final ClassValue<Enum<?>[]> KEY_UNIVERSE_CACHE = new ClassValue<>() {
		@Override
		protected Enum<?>[] computeValue(Class<?> type) {
			Enum<?>[] universe = (Enum<?>[]) type.getEnumConstants();
			if (universe.length > 320) {
				throw new IllegalStateException("BaseMap supports at most 320 enum constants, got " + universe.length + " for " + type.getName());
			}
			return universe;
		}
	};
	private static final Object[] EMPTY_VALUES = new Object[0];
	private static final SharedData<?> EMPTY_DATA = new SharedData<>(EMPTY_VALUES, 0L, 0L, 0L, 0L, 0L, true);

	private transient boolean readOnly;
	private final Class<K> keyType;
	private final K[] keyUniverse;
	private SharedData<V> data;
	private transient Set<Entry<K, V>> entrySet;

	@SuppressWarnings("unchecked")
	public BaseMap(Class<K> keyType) {
		this.keyType = keyType;
		this.keyUniverse = (K[]) KEY_UNIVERSE_CACHE.get(keyType);
		this.data = (SharedData<V>) EMPTY_DATA;
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

	@Override
	@SuppressWarnings("unchecked")
	public V getOrDefault(Object key, V defaultValue) {
		SharedData<V> d = this.data;
		if (d == EMPTY_DATA) {
			return defaultValue;
		}
		int ordinal = ordinalOf(key);
		if (ordinal < 0 || !d.isPresent(ordinal)) {
			return defaultValue;
		}
		return (V) d.values[d.indexOf(ordinal)];
	}

	protected void checkNotFrozen() {
		if (readOnly) {
			throw new UnsupportedOperationException("This object is frozen (read-only) and cannot be modified.");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public V put(K key, V value) {
		checkNotFrozen();
		int ordinal = key.ordinal();
		SharedData<V> d = this.data;
		int idx = d.indexOf(ordinal);
		if (d.isPresent(ordinal)) {
			V previous = (V) d.values[idx];
			if (d.shared) {
				Object[] newValues = d.values.clone();
				newValues[idx] = value;
				this.data = new SharedData<>(newValues, d.b0, d.b1, d.b2, d.b3, d.b4, false);
			} else {
				d.values[idx] = value;
			}
			return previous;
		} else {
			Object[] oldValues = d.values;
			int len = oldValues.length;
			Object[] newValues = new Object[len + 1];
			if (idx > 0) {
				System.arraycopy(oldValues, 0, newValues, 0, idx);
			}
			newValues[idx] = value;
			if (idx < len) {
				System.arraycopy(oldValues, idx, newValues, idx + 1, len - idx);
			}
			long bit = 1L << ordinal;
			long nb0 = d.b0, nb1 = d.b1, nb2 = d.b2, nb3 = d.b3, nb4 = d.b4;
			switch (ordinal >>> 6) {
				case 0 -> nb0 |= bit;
				case 1 -> nb1 |= bit;
				case 2 -> nb2 |= bit;
				case 3 -> nb3 |= bit;
				default -> nb4 |= bit;
			}
			if (d.shared) {
				this.data = new SharedData<>(newValues, nb0, nb1, nb2, nb3, nb4, false);
			} else {
				d.values = newValues;
				d.b0 = nb0;
				d.b1 = nb1;
				d.b2 = nb2;
				d.b3 = nb3;
				d.b4 = nb4;
			}
			return null;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		checkNotFrozen();
		SharedData<V> d = this.data;
		if (d == EMPTY_DATA) {
			return null;
		}
		int ordinal = ordinalOf(key);
		if (ordinal < 0) {
			return null;
		}
		return removeByOrdinal(ordinal);
	}

	@SuppressWarnings("unchecked")
	protected final V removeByOrdinal(int ordinal) {
		SharedData<V> d = this.data;
		if (!d.isPresent(ordinal)) {
			return null;
		}
		int idx = d.indexOf(ordinal);
		Object[] oldValues = d.values;
		V previous = (V) oldValues[idx];
		int len = oldValues.length;
		if (len == 1) {
			this.data = (SharedData<V>) EMPTY_DATA;
			return previous;
		}
		Object[] newValues = new Object[len - 1];
		if (idx > 0) {
			System.arraycopy(oldValues, 0, newValues, 0, idx);
		}
		if (idx < len - 1) {
			System.arraycopy(oldValues, idx + 1, newValues, idx, len - idx - 1);
		}
		long mask = ~(1L << ordinal);
		long nb0 = d.b0, nb1 = d.b1, nb2 = d.b2, nb3 = d.b3, nb4 = d.b4;
		switch (ordinal >>> 6) {
			case 0 -> nb0 &= mask;
			case 1 -> nb1 &= mask;
			case 2 -> nb2 &= mask;
			case 3 -> nb3 &= mask;
			default -> nb4 &= mask;
		}
		if (d.shared) {
			this.data = new SharedData<>(newValues, nb0, nb1, nb2, nb3, nb4, false);
		} else {
			d.values = newValues;
			d.b0 = nb0;
			d.b1 = nb1;
			d.b2 = nb2;
			d.b3 = nb3;
			d.b4 = nb4;
		}
		return previous;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void clear() {
		checkNotFrozen();
		this.data = (SharedData<V>) EMPTY_DATA;
	}

	@Override
	@SuppressWarnings("unchecked")
	public V get(Object key) {
		SharedData<V> d = this.data;
		if (d == EMPTY_DATA) {
			return null;
		}
		int ordinal = ordinalOf(key);
		if (ordinal < 0 || !d.isPresent(ordinal)) {
			return null;
		}
		return (V) d.values[d.indexOf(ordinal)];
	}

	@SuppressWarnings("unchecked")
	protected final V getByOrdinal(int ordinal) {
		SharedData<V> d = this.data;
		if (d == EMPTY_DATA || !d.isPresent(ordinal)) {
			return null;
		}
		return (V) d.values[d.indexOf(ordinal)];
	}

	@Override
	public boolean containsKey(Object key) {
		SharedData<V> d = this.data;
		if (d == EMPTY_DATA) {
			return false;
		}
		int ordinal = ordinalOf(key);
		return ordinal >= 0 && d.isPresent(ordinal);
	}

	protected final boolean containsOrdinal(int ordinal) {
		SharedData<V> d = this.data;
		return d != EMPTY_DATA && d.isPresent(ordinal);
	}

	@Override
	public int size() {
		return data.values.length;
	}

	@Override
	public boolean isEmpty() {
		return data.values.length == 0;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		checkNotFrozen();
		if (m == null || m.isEmpty()) {
			return;
		}
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
		if (!data.shared) {
			data.shared = true;
		}
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	@SuppressWarnings("unchecked")
	public BaseMap<K, V> clone() {
		try {
			BaseMap<K, V> clone = (BaseMap<K, V>) super.clone();
			if (!this.data.shared) {
				this.data.shared = true;
			}
			clone.data = this.data;
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

	private int ordinalOf(Object key) {
		if (key instanceof Enum<?> e && e.getDeclaringClass() == keyType) {
			return e.ordinal();
		}
		return -1;
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
		@SuppressWarnings("unchecked")
		public V getValue() {
			SharedData<V> d = data;
			return d.isPresent(ordinal) ? (V) d.values[d.indexOf(ordinal)] : null;
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
		if (fromIndex >= 320) {
			return -1;
		}
		SharedData<V> d = this.data;
		int wordIndex = fromIndex >>> 6;
		long word = d.word(wordIndex) & (~0L << fromIndex);
		while (true) {
			if (word != 0L) {
				return (wordIndex << 6) + Long.numberOfTrailingZeros(word);
			}
			if (++wordIndex >= 5) {
				return -1;
			}
			word = d.word(wordIndex);
		}
	}

	private static final class SharedData<V> implements Serializable {
		private Object[] values;
		private long b0, b1, b2, b3, b4;
		private boolean shared;

		private SharedData(Object[] values, long b0, long b1, long b2, long b3, long b4, boolean shared) {
			this.values = values;
			this.b0 = b0;
			this.b1 = b1;
			this.b2 = b2;
			this.b3 = b3;
			this.b4 = b4;
			this.shared = shared;
		}

		private long word(int wordIndex) {
			return switch (wordIndex) {
				case 0 -> b0;
				case 1 -> b1;
				case 2 -> b2;
				case 3 -> b3;
				default -> b4;
			};
		}

		private boolean isPresent(int ordinal) {
			long word = switch (ordinal >>> 6) {
				case 0 -> b0;
				case 1 -> b1;
				case 2 -> b2;
				case 3 -> b3;
				default -> b4;
			};
			return (word & (1L << ordinal)) != 0L;
		}

		private int indexOf(int ordinal) {
			long mask = (1L << ordinal) - 1L;
			return switch (ordinal >>> 6) {
				case 0 -> Long.bitCount(b0 & mask);
				case 1 -> Long.bitCount(b0) + Long.bitCount(b1 & mask);
				case 2 -> Long.bitCount(b0) + Long.bitCount(b1) + Long.bitCount(b2 & mask);
				case 3 -> Long.bitCount(b0) + Long.bitCount(b1) + Long.bitCount(b2) + Long.bitCount(b3 & mask);
				default -> Long.bitCount(b0) + Long.bitCount(b1) + Long.bitCount(b2) + Long.bitCount(b3) + Long.bitCount(b4 & mask);
			};
		}
	}
}
