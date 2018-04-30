package net.demilich.metastone.game.utils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An attribute map is a {@link Map} that contains {@link Attribute} as keys and {@link Object} (typically integers and
 * booleans) as values. For example, the attributes of a minion that has "Divine Shield. Spell Damage +1" looks like:
 * <pre>
 *   {
 *     "DIVINE_SHIELD": true,
 *     "SPELL_DAMAGE": 1
 *   }
 * </pre>
 * Observe that the key names are capitalized and have underscores, exactly like they appear in {@link Attribute}.
 * <p>
 * Attributes should store whatever is meant to be persisted throughout the game. They can be affected by spells like
 * {@link net.demilich.metastone.game.spells.AddAttributeSpell} and {@link net.demilich.metastone.game.spells.RemoveAttributeSpell};
 * they are modified en-mass by {@link net.demilich.metastone.game.spells.SilenceSpell}. Some attributes, like {@link
 * Attribute#MANA_COST_MODIFIER}, contain not an integer or boolean but a proper object, like a {@link
 * net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider} in this case.
 *
 * @see Attribute for more about valid attributes here.
 */
public class AttributeMap extends ConcurrentHashMap<Attribute, Object> implements Serializable, Cloneable {
	public AttributeMap() {
		super();
	}

	public AttributeMap(Map<Attribute, Object> attributes) {
		super(attributes);
	}

	@Override
	public AttributeMap clone() {
		AttributeMap map = new AttributeMap();
		synchronized (this) {
			map.putAll(this);
		}
		return map;
	}

	@Override
	public Object put(@NotNull Attribute key, Object value) {
		if (value == null && this.containsKey(key)) {
			throw new IllegalStateException("Cannot clear a key with a null value");
		}
		if (value == null) {
			return null;
		}
		return super.put(key, value);
	}

	public Set<Attribute> unsafeKeySet() {
		return keySet();
	}

	@Override
	public Set<Entry<Attribute, Object>> entrySet() {
		return super.entrySet();
	}
}
