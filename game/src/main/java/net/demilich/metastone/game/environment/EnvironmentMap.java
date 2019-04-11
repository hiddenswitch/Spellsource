package net.demilich.metastone.game.environment;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * A key value store held in the environment.
 *
 * @param <K> The key type, typically immutable
 * @param <E> The value type, typically immutable.
 */
public class EnvironmentMap<K extends Serializable, E extends Serializable> extends LinkedHashMap<K, E> implements EnvironmentValue, Serializable {

	@Override
	public EnvironmentValue getCopy() {
		EnvironmentMap<K, E> copy = new EnvironmentMap<>();
		copy.putAll(this);
		return copy;
	}
}
