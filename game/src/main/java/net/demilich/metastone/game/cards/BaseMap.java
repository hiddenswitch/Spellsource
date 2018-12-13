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
public abstract class BaseMap<K extends Enum<K>, V> extends EnumMap<K, V> {
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

	@Override
	public BaseMap<K, V> clone() {
		return (BaseMap<K, V>) super.clone();
	}
}
