package net.demilich.metastone.game.utils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bberman on 2/4/17.
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

}
