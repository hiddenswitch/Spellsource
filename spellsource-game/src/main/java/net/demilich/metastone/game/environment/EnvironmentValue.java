package net.demilich.metastone.game.environment;

import java.io.Serializable;

/**
 * A copyable, single value in the environment.
 */
public interface EnvironmentValue extends Cloneable, Serializable {
	EnvironmentValue getCopy();
}

