package net.demilich.metastone.game;

import java.io.Serializable;
import java.util.Stack;

public class EnvironmentStack<E extends Serializable> extends Stack<E> implements EnvironmentValue, Serializable {
	@Override
	public EnvironmentValue getCopy() {
		EnvironmentStack<E> copy = new EnvironmentStack<>();
		copy.addAll(this);
		return copy;
	}
}
