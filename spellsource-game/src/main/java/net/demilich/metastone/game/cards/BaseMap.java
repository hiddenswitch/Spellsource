package net.demilich.metastone.game.cards;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A base map wrapping extending a {@link HashMap} or {@link EnumMap} depending on the memory needs of this server
 * instance.
 *
 * @param <K>
 * @param <V>
 */
public abstract class BaseMap<K extends Enum<K>, V> extends EnumMap<K, V> implements Freezable {
	private transient boolean readOnly;

	public BaseMap(Class<K> keyType) {
		// To support an enum map base, enable the super below
		super(keyType);
	}

	BaseMap(Map<K, V> m) {
		super(m);
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
		return super.put(key, value);
	}

	@Override
	public V remove(Object key) {
		checkNotFrozen();
		return super.remove(key);
	}

	@Override
	public void clear() {
		checkNotFrozen();
		super.clear();
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
		BaseMap<K, V> clone = (BaseMap<K, V>) super.clone();
		clone.readOnly = this.readOnly;
		return clone;
	}

	/**
	 * Creates a deep copy with readOnly set to false.
	 */
	public BaseMap<K, V> cloneAndUnfreeze() {
		BaseMap<K, V> clone = (BaseMap<K, V>) super.clone();
		clone.readOnly = false;
		return clone;
	}
}
