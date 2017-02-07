package net.demilich.metastone.game.utils;

import net.demilich.metastone.game.Attribute;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by bberman on 2/4/17.
 */
public class AttributeMap implements Map<Attribute, Object> {
	public static final AttributeMap EMPTY = new AttributeMap();
	private EnumMap<Attribute, Object> attributes = new EnumMap<>(Attribute.class);

	@Override
	public int size() {
		return attributes.size();
	}

	@Override
	public boolean isEmpty() {
		// TODO: Network attributes count too
		return attributes.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return attributes.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return attributes.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		// TODO: Call appropriate context methods to get networked information
		return attributes.get(key);
	}

	@Override
	public Object put(Attribute key, Object value) {
		// TODO: Save an attribute
		return attributes.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return attributes.remove(key);
	}

	@Override
	public void putAll(Map<? extends Attribute, ?> m) {
		attributes.putAll(m);
	}

	@Override
	public void clear() {
		attributes.clear();
	}

	@Override
	public Set<Attribute> keySet() {
		return attributes.keySet();
	}

	@Override
	public Collection<Object> values() {
		return attributes.values();
	}

	@Override
	public Set<Entry<Attribute, Object>> entrySet() {
		return attributes.entrySet();
	}
}
