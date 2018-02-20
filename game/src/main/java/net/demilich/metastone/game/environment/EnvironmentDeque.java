package net.demilich.metastone.game.environment;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;

public class EnvironmentDeque<E extends Serializable> extends ArrayDeque<E> implements EnvironmentValue, Serializable {
	@Override
	public EnvironmentValue getCopy() {
		EnvironmentDeque<E> copy = new EnvironmentDeque<>();
		copy.addAll(this);
		return copy;
	}
}
