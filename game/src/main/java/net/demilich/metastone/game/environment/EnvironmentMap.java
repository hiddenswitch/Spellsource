package net.demilich.metastone.game.environment;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class EnvironmentMap<K extends Serializable, E extends Serializable> extends LinkedHashMap<K, E> implements EnvironmentValue, Serializable {

	@Override
	public EnvironmentValue getCopy() {
		EnvironmentMap<K, E> copy = new EnvironmentMap<>();
		copy.putAll(this);
		return copy;
	}
}
