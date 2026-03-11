package net.demilich.metastone.game.cards.desc;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;

import net.demilich.metastone.game.cards.BaseMap;
import net.demilich.metastone.game.cards.Freezable;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * A map representing a complex type in Spellsource, like a {@link net.demilich.metastone.game.spells.Spell} or {@link
 * net.demilich.metastone.game.spells.desc.condition.Condition}.
 *
 * @param <T> The enum representing the parameters/arguments/fields in the abstract base class.
 * @param <V> The abstract base class of the concrete type.
 * @see DescDeserializer for a walk through on how deserialization of card JSON works on subclasses of this one.
 */
@JsonSerialize(using = DescSerializer.class)
public abstract class Desc<T extends Enum<T>, V extends HasDesc<?>> extends BaseMap<T, Object> implements Serializable,
		Cloneable, HasDesc<Desc<T, V>>, HasEntrySet<T, Object> {

	protected Desc(Map<T, Object> arguments, Class<T> keyType) {
		super(keyType);
		if (arguments.isEmpty()) {
			return;
		}
		putAll(arguments);
	}

	protected Desc(Class<T> keyType) {
		super(keyType);
	}

	public Desc(Class<? extends V> clazz, Class<T> keyType) {
		super(keyType);
		put(getClassArg(), clazz);
	}

	@SuppressWarnings("unchecked")
	public Class<? extends V> getDescClass() {
		return (Class<? extends V>) get(getClassArg());
	}

	protected abstract Class<? extends Desc> getDescImplClass();

	/**
	 * Per-instance memoized desc create instance.
	 *
	 * @return An instance of the underlying implementation of this desc.
	 */
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
		checkNotFrozen();
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
		for (T arg : keySet()) {
			Object value = get(arg);
			if (value instanceof CustomCloneable) {
				CustomCloneable cloneable = (CustomCloneable) value;
				clone.put(arg, cloneable.clone());
			} else if (value instanceof Desc) {
				Desc descClone = (Desc) value;
				clone.put(arg, descClone.clone());
			} else {
				clone.put(arg, value);
			}
		}
		clone.setReadOnly(this.isReadOnly());
		return clone;
	}

	@Override
	public void freeze() {
		super.freeze();
		for (Object value : values()) {
			freezeValue(value);
		}
	}

	private static void freezeValue(Object value) {
		if (value instanceof Freezable) {
			((Freezable) value).freeze();
		} else if (value instanceof Object[]) {
			for (Object element : (Object[]) value) {
				freezeValue(element);
			}
		}
	}

	/**
	 * Creates a deep copy of this desc with readOnly set to false, recursively unfreezing all nested values.
	 */
	public Desc<T, V> cloneAndUnfreeze() {
		Desc<T, V> clone = (Desc<T, V>) super.cloneAndUnfreeze();
		// Deep copy all nested mutable values
		for (T arg : clone.keySet()) {
			Object value = clone.get(arg);
			Object unfrozen = deepCopyAndUnfreeze(value);
			if (unfrozen != value) {
				// bypass frozen check by using super.put directly since clone is already unfrozen
				clone.put(arg, unfrozen);
			}
		}
		return clone;
	}

	@SuppressWarnings("unchecked")
	private static Object deepCopyAndUnfreeze(Object value) {
		if (value instanceof Desc) {
			return ((Desc<?, ?>) value).cloneAndUnfreeze();
		} else if (value instanceof CustomCloneable) {
			Object cloned = ((CustomCloneable) value).clone();
			if (cloned instanceof Freezable) {
				unfreezeRecursive(cloned);
			}
			return cloned;
		} else if (value instanceof Object[]) {
			Object[] arr = (Object[]) value;
			Object[] newArr = arr.clone();
			boolean changed = false;
			for (int i = 0; i < newArr.length; i++) {
				Object orig = newArr[i];
				Object copy = deepCopyAndUnfreeze(orig);
				if (copy != orig) {
					newArr[i] = copy;
					changed = true;
				}
			}
			return changed ? newArr : newArr;
		}
		return value;
	}

	private static void unfreezeRecursive(Object value) {
		if (value instanceof BaseMap) {
			((BaseMap<?, ?>) value).setReadOnly(false);
			for (Object v : ((BaseMap<?, ?>) value).values()) {
				unfreezeRecursive(v);
			}
		} else if (value instanceof Object[]) {
			for (Object element : (Object[]) value) {
				unfreezeRecursive(element);
			}
		}
	}

	@Override
	public Desc<T, V> getDesc() {
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setDesc(Desc<?, ?> desc) {
		checkNotFrozen();
		this.clear();
		this.putAll((Map) desc);
	}
}
