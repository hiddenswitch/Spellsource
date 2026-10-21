package net.demilich.metastone.game.environment;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;

/**
 * A stack in the environment.
 *
 * @param <E> The object type that is held in the stack (typically primitive or immutable).
 */
public class EnvironmentDeque<E extends Serializable> extends ArrayDeque<E> implements EnvironmentValue, Serializable {
	public EnvironmentDeque() {
		super(4);
	}

	public EnvironmentDeque(Collection<? extends E> c) {
		super(c);
	}

	@Override
	public EnvironmentValue getCopy() {
		return (EnvironmentValue) super.clone();
	}
}
