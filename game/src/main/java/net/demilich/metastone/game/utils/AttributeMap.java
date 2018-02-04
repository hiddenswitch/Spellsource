package net.demilich.metastone.game.utils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by bberman on 2/4/17.
 */
public class AttributeMap extends HashMap<Attribute, Object> implements Serializable {
	public AttributeMap() {
		super();
	}

	public AttributeMap(Map<Attribute, Object> attributes) {
		super(attributes);
	}
}
