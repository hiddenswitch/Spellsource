package net.demilich.metastone.game.cards.desc;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A map representing a complex type in Spellsource, like a {@link net.demilich.metastone.game.spells.Spell} or {@link
 * net.demilich.metastone.game.spells.desc.condition.Condition}.
 *
 * @param <T> The enum representing the parameters/arguments/fields in the abstract base class.
 * @param <V> The abstract base class of the concrete type.
 */
@JsonSerialize(using = DescSerializer.class)
public abstract class Desc<T extends Enum<T>, V extends HasDesc<?>> extends ConcurrentHashMap<T, Object> implements Serializable, Cloneable, HasDesc<Desc<T, V>> {
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

	public V create() {
		Class<? extends V> clazz = getDescClass();
		try {
			return clazz.getConstructor(getDescImplClass()).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// Try a no-args constructor and set the desc
			try {
				final V v = getDescClass().getConstructor().newInstance();
				v.setDesc(this);
				return v;
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e1) {
				throw new RuntimeException(e1);
			}
		}
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
		return super.equals(other);
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

	@Override
	public Desc<T, V> getDesc() {
		return this;
	}

	@Override
	public void setDesc(Desc<?, ?> desc) {
		this.clear();
		this.putAll((Map) desc);
	}
}
