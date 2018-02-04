package net.demilich.metastone.game.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.demilich.metastone.game.logic.GameLogic;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.Serializable;
import java.util.*;

/**
 * Created by bberman on 2/4/17.
 */
public final class AttributeMap implements Serializable, Cloneable {
	private static final String CANNOT_REVERT_MESSAGE = "Cannot revert an effect that was never added to this AttributeMap instance.";

	private int nextId = 0;
	private final ListValuedMap<Attribute, AttributeEntry> attrs;
	private Map<Attribute, Object> cache;

	public AttributeMap() {
		attrs = new ArrayListValuedHashMap<>();
		cache = new HashMap<>();
	}

	public AttributeMap(AttributeMap attributes) {
		this();
		cache.putAll(attributes.cache);
		for (Map.Entry<Attribute, AttributeEntry> c : attributes.entries()) {
			attrs.put(c.getKey(), c.getValue().clone());
		}
	}

	@Override
	public AttributeMap clone() {
		return new AttributeMap(this);
	}

	public Set<Map.Entry<Attribute, Object>> entrySet() {
		return Collections.unmodifiableSet(cache.entrySet());
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Attribute key) {
		return (T) cache.get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T getOrDefault(Attribute key, T defaultValue) {
		return (T) cache.getOrDefault(key, defaultValue);
	}

	public void silence() {
		// TODO: We're retiring the distinction between AURA and non-AURA attributes
		innerSilence();

		// Reset values
		resetCache();
	}

	protected void innerSilence() {
		MapIterator<Attribute, AttributeEntry> iterator = attrs.mapIterator();
		while (iterator.hasNext()) {
			Attribute key = iterator.next();
			AttributeEntry value = iterator.getValue();
			if (GameLogic.IMMUNE_TO_SILENCE.contains(key)) {
				continue;
			}

			if (value.type == Type.AURA) {
				continue;
			}

			value.silenced = true;
		}
	}

	@Deprecated
	public Set<Attribute> keySet() {
		return attrs.keySet();
	}

	@Deprecated
	public boolean containsKey(Attribute attribute) {
		return attrs.containsKey(attribute);
	}

	protected void resetCache() {
		cache.clear();
		MapIterator<Attribute, AttributeEntry> iterator;
		iterator = attrs.mapIterator();
		while (iterator.hasNext()) {
			Attribute key = iterator.next();
			AttributeEntry value = iterator.getValue();
			if (value.silenced) {
				continue;
			}
			// Sets will always override the cache
			// Adds will add on if there's a prior value in the cache, otherwise overrides the cache
			innerUpdate(key, value);
		}
	}

	protected void resetCache(Attribute key) {
		cache.remove(key);
		Iterator<AttributeEntry> iterator = attrs.get(key).iterator();
		while (iterator.hasNext()) {
			AttributeEntry value = iterator.next();
			if (value.silenced) {
				continue;
			}
			innerUpdate(key, value);
		}
	}

	private void innerUpdate(Attribute key, AttributeEntry value) {
		if (value.operation == Operation.SET
				|| (value.operation == Operation.ADD
				&& cache.computeIfPresent(key, (ignored, existing) -> (int) existing + (int) value.value) == null)) {
			cache.put(key, value.value);
		}
	}

	public void textSet(Attribute attribute, JsonElement element) {
		JsonPrimitive primitive = element.getAsJsonPrimitive();

		if (primitive.isBoolean()) {
			textSet(attribute, element.getAsBoolean());
		} else if (primitive.isNumber()) {
			textSet(attribute, element.getAsInt());
		} else if (primitive.isString()) {
			textSet(attribute, element.getAsString());
		}
	}

	/**
	 * Puts (sets) the {@link Attribute} to {@code value} as an enchantment.
	 *
	 * @param key   The {@link Attribute} to set.
	 * @param value The new value of the {@link Attribute}.
	 * @return An ID that can be used to undo the setting.
	 * @see #revert(int) to see how to revert an effect with the given ID.
	 */
	public int put(Attribute key, Object value) {
		return enchantSet(key, value);
	}

	/**
	 * Sets the text (default) attribute on this attribute map.
	 * <p>
	 * For {@link Integer} values, the starting value is assumed {@code 0} unless specified otherwise.
	 * <p>
	 * Text sets should not be considered revertible, so their IDs are never returned.
	 *
	 * @param key   An {@link Attribute}
	 * @param value The starting value of the {@link Attribute}.
	 * @return The set value.
	 */
	public int textSet(Attribute key, int value) {
		put(key, Type.TEXT, Operation.SET, value, value);
		return value;
	}

	/**
	 * Sets the text (default) attribute on this attribute map.
	 * <p>
	 * For {@link String} values, the starting value is assumed {@code null} unless specified otherwise.
	 *
	 * @param key
	 * @param value
	 * @return The set value.
	 */
	public String textSet(Attribute key, String value) {
		put(key, Type.TEXT, Operation.SET, value, value);
		return value;
	}


	/**
	 * Sets the text (default) attribute on this attribute map.
	 * <p>
	 * For {@link Boolean} values, the starting value is assumed {@code false} unless specified otherwise.
	 * <p>
	 * Text sets should not be considered revertible, so their IDs are never returned.
	 *
	 * @param key   An {@link Attribute}
	 * @param value The starting value of the {@link Attribute}.
	 * @return The set value.
	 */
	public boolean textSet(Attribute key, boolean value) {
		put(key, Type.TEXT, Operation.SET, value, value);
		return value;
	}

	/**
	 * Enchants the attribute map to set the given {@link Attribute} to the given value.
	 *
	 * @param key   The {@link Attribute} to set.
	 * @param value The new value of the {@link Attribute}.
	 * @return An ID that can be used to undo the setting.
	 * @see #revert(int) to see how to revert an effect with the given ID.
	 */
	public int enchantSet(Attribute key, Object value) {
		return put(key, Type.ENCHANTMENT, Operation.SET, value, value);
	}

	/**
	 * Reverts a specific effect.
	 *
	 * @param id  The ID of the effect to revert.
	 * @param <T> The attribute type.
	 * @return The new value of the attribute herein reverted.
	 */
	public <T> T revert(int id) {
		MapIterator<Attribute, AttributeEntry> iterator = attrs.mapIterator();
		boolean removed = false;
		Attribute key = null;
		while (iterator.hasNext()) {
			key = iterator.next();
			AttributeEntry value = iterator.getValue();
			if (value.id == id) {
				// Remove it, and reset the cache
				iterator.remove();
				removed = true;
				break;
			}
		}

		if (!removed
				|| key == null) {
			throw new IllegalArgumentException(CANNOT_REVERT_MESSAGE);
		}

		resetCache(key);
		return get(key);
	}

	/**
	 * A faster revert, when the attribute is known.
	 *
	 * @param attribute The attribute that contains the {@code id}.
	 * @param id        The ID of the effect to remove.
	 * @param <T>       The return type of the attribute.
	 * @return The new value of the attribute.
	 * @see #revert(int) for the more general function.
	 */
	public <T> T revert(Attribute attribute, int id) {
		Iterator<AttributeEntry> iterator = attrs.get(attribute).iterator();
		boolean removed = false;
		while (iterator.hasNext()) {
			if (iterator.next().id == id) {
				iterator.remove();
				removed = true;
				break;
			}
		}

		if (!removed) {
			throw new IllegalArgumentException(CANNOT_REVERT_MESSAGE);
		}

		resetCache(attribute);
		return get(attribute);
	}


	/**
	 * Adds the {@code value} to the specified {@link Attribute}.
	 *
	 * @param intKey An {@link Attribute} that stores {@link Integer} values.
	 * @param value  The value to add.
	 * @return An ID for this effect.
	 */
	public int add(Attribute intKey, int value) {
		int newValue = (int) cache.getOrDefault(intKey, 0) + value;
		put(intKey, Type.ENCHANTMENT, Operation.ADD, value, newValue);
		return newValue;
	}

	public int auraAdd(Attribute intKey, int value) {
		int newValue = (int) cache.getOrDefault(intKey, 0) + value;
		put(intKey, Type.AURA, Operation.ADD, value, newValue);
		return newValue;
	}

	public int auraSet(Attribute key, int value) {
		return put(key, Type.AURA, Operation.SET, value, value);
	}

	public int auraSet(Attribute key, boolean value) {
		return put(key, Type.AURA, Operation.SET, value, value);
	}

	@Deprecated
	public boolean remove(Attribute attribute) {
		cache.remove(attribute);
		return attrs.remove(attribute).size() > 0;
	}

	public boolean has(Attribute key) {
		if (cache.containsKey(key)) {
			Object o = cache.get(key);
			if (o instanceof Integer) {
				return (int) o != 0;
			} else if (o instanceof Boolean) {
				return (boolean) o;
			}
			return true;
		}
		return false;
	}

	protected int put(Attribute key, Type type, Operation operation, Object value, Object cacheValue) {
		AttributeEntry entry = new AttributeEntry(type, operation, value);
		attrs.put(key, entry);
		cache.put(key, cacheValue);
		return entry.id;
	}

	private Collection<Map.Entry<Attribute, AttributeEntry>> entries() {
		return attrs.entries();
	}

	public AttributeMap putAll(AttributeMap map) {
		attrs.putAll(map.attrs);
		resetCache();
		return this;
	}

	private enum Type {
		TEXT,
		ENCHANTMENT,
		AURA
	}

	private enum Operation {
		ADD,
		SET
	}

	private final class AttributeEntry implements Cloneable, Serializable {
		private final int id;
		private final Type type;
		private final Operation operation;
		private final Object value;
		private boolean silenced;

		private AttributeEntry(Type type, Operation operation, Object value) {
			this.id = nextId++;
			this.type = type;
			this.operation = operation;
			this.value = value;
		}

		@Override
		protected AttributeEntry clone() {
			try {
				return (AttributeEntry) super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public boolean equals(Object obj) {
			return id == ((AttributeEntry) obj).id;
		}
	}
}
