package net.demilich.metastone.gui.common;

import javafx.util.StringConverter;
import net.demilich.metastone.game.behaviour.Behaviour;

public class BehaviourStringConverter extends StringConverter<Behaviour> {

	@Override
	public Behaviour fromString(String string) {
		return null;
	}

	@Override
	public String toString(Behaviour behaviour) {
		return behaviour.getName();
	}

}