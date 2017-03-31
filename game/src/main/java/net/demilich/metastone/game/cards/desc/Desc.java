package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Map;

public class Desc<T> implements Serializable {

	protected final Map<T, Object> arguments;

	public Desc(Map<T, Object> arguments) {
		this.arguments = arguments;
	}

	public boolean contains(T arg) {
		return arguments.containsKey(arg);
	}

	public Object get(T arg) {
		return arguments.get(arg);
	}

	public boolean getBool(T arg) {
		return arguments.containsKey(arg) ? (boolean) get(arg) : false;
	}

	public int getInt(T arg) {
		return arguments.containsKey(arg) ? (int) get(arg) : 0;
	}

	public String getString(T arg) {
		return arguments.containsKey(arg) ? (String) get(arg) : "";
	}

	public int getValue(T arg, GameContext context, Player player, Entity target, Entity host, int defaultValue) {
		Object storedValue = arguments.get(arg);
		if (storedValue == null) {
			return defaultValue;
		}
		if (storedValue instanceof ValueProvider) {
			ValueProvider valueProvider = (ValueProvider) storedValue;
			return valueProvider.getValue(context, player, target, host);
		}
		return (int) storedValue;
	}

	public String getClassName() {
		final String simpleName = ((Class) arguments.get("class")).getSimpleName();
		return simpleName;
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
		if (arguments == null && rhs.arguments != null) {
			return false;
		}
		if (rhs.arguments == null || rhs.arguments.size() != arguments.size()) {
			return false;
		}
		EqualsBuilder eq = new EqualsBuilder();
		for (Map.Entry entry : arguments.entrySet()) {
			final Object left = entry.getValue();
			final Object right = rhs.arguments.get(entry.getKey());
			eq.append(left, right);
		}
		return eq.isEquals();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(113, 71);
		for (Map.Entry entry : arguments.entrySet()) {
			builder.append(entry.hashCode());
		}
		return builder.toHashCode();
	}
}
