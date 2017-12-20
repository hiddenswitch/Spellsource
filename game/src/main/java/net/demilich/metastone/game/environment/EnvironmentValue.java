package net.demilich.metastone.game.environment;

import java.io.Serializable;

public interface EnvironmentValue extends Cloneable, Serializable {
	EnvironmentValue getCopy();
}

