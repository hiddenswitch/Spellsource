package net.demilich.metastone.game.environment;

import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A stack in the environment.
 *
 * @param <E> The object type that is held in the stack (typically primitive or immutable).
 */
public class EnvironmentDeque<E extends Serializable> extends ConcurrentLinkedDeque<E> implements EnvironmentValue, Serializable {
	@Override
	public EnvironmentValue getCopy() {
		EnvironmentDeque<E> copy = new EnvironmentDeque<>();
		copy.addAll(this);
		return copy;
	}
}
