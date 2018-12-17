package net.demilich.metastone.game.environment;

import java.io.Serializable;
import java.util.ArrayDeque;

/**
 * A stack in the environment.
 *
 * @param <E> The object type that is held in the stack (typically primitive or immutable).
 */
public class EnvironmentDeque<E extends Serializable> extends ArrayDeque<E> implements EnvironmentValue, Serializable {
	private static final long serialVersionUID = -6537459227691819805L;

	@Override
	public EnvironmentValue getCopy() {
		EnvironmentDeque<E> copy = new EnvironmentDeque<>();
		copy.addAll(this);
		return copy;
	}
}
