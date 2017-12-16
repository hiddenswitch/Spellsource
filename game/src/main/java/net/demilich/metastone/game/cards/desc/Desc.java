package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A card or card component description base class.
 * <p>
 * This {@link Map} type has typed {@link Enum} keys and
 *
 * @param <T>
 */
public class Desc<T extends Enum> extends HashMap<T, Object> implements Serializable, Cloneable {
	public Desc(Map<T, Object> arguments) {
		super(arguments);
	}

	public boolean getBool(T arg) {
		return containsKey(arg) && (boolean) get(arg);
	}

	public int getInt(T arg) {
		return containsKey(arg) ? (int) get(arg) : 0;
	}

	public String getString(T arg) {
		return containsKey(arg) ? (String) get(arg) : "";
	}

	public int getValue(T arg, GameContext context, Player player, Entity target, Entity host, int defaultValue) {
		Object storedValue = this.get(arg);
		if (storedValue == null) {
			return defaultValue;
		}
		if (ValueProvider.class.isAssignableFrom(storedValue.getClass())) {
			ValueProvider valueProvider = (ValueProvider) storedValue;
			return valueProvider.getValue(context, player, target, host);
		}
		return (int) storedValue;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (!Desc.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		Desc rhs = (Desc) other;
		if (rhs.size() != this.size()) {
			return false;
		}
		EqualsBuilder eq = new EqualsBuilder();
		for (Map.Entry entry : this.entrySet()) {
			final Object left = entry.getValue();
			final Object right = this.get(entry.getKey());
			eq.append(left, right);
		}
		return eq.isEquals();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(113, 71);
		for (Map.Entry entry : this.entrySet()) {
			builder.append(entry.hashCode());
		}
		return builder.toHashCode();
	}
}
