package net.demilich.metastone.game.cards.desc;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A card or card component description base class.
 * <p>
 * This {@link Map} type has typed {@link Enum} keys and
 *
 * @param <T>
 */
public abstract class Desc<T extends Enum, V> extends ConcurrentHashMap<T, Object> implements Serializable, Cloneable {
	protected Desc(Map<T, Object> arguments) {
		super(arguments);
	}

	protected Desc() {
		super();
	}

	public Desc(Class<? extends V> clazz) {
		super();
		put(getClassArg(), clazz);
	}

	@SuppressWarnings("unchecked")
	public Class<? extends V> getDescClass() {
		return (Class<? extends V>) get(getClassArg());
	}

	protected abstract Class<? extends Desc> getDescImplClass();

	public V createInstance() {
		Class<? extends V> clazz = getDescClass();
		try {
			return clazz.getConstructor(getDescImplClass()).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	public abstract T getClassArg();

	public boolean getBool(T arg) {
		return containsKey(arg) && (boolean) get(arg);
	}

	public int getInt(T arg) {
		return containsKey(arg) ? (int) get(arg) : 0;
	}

	public String getString(T arg) {
		return containsKey(arg) ? (String) get(arg) : "";
	}

	@Suspendable
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

	@Override
	public Object put(@NotNull T key, Object value) {
		if (value == null && this.containsKey(key)) {
			throw new IllegalStateException("Cannot clear a key with a null value");
		}
		if (value == null) {
			return null;
		}
		return super.put(key, value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public abstract Desc<T, V> clone();

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.toString();
	}

	protected Desc<T, V> copyTo(Desc<T, V> clone) {
		synchronized (this) {
			for (T arg : keySet()) {
				Object value = get(arg);
				if (value instanceof CustomCloneable) {
					CustomCloneable cloneable = (CustomCloneable) value;
					clone.put(arg, cloneable.clone());
				} else {
					clone.put(arg, value);
				}
			}
		}
		return clone;
	}
}
