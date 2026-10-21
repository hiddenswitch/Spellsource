package net.demilich.metastone.game.behaviour;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.logic.XORShiftRandom;

public class PlayGameLogicRandomBehaviour extends PlayRandomBehaviour {

	@Override
	public String getName() {
		return "Play Random Reproducibly";
	}

	@Override
	protected XORShiftRandom getRandom(GameContext context) {
		return context.getLogic().getRandom();
	}
}
