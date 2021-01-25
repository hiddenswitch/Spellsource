package net.demilich.metastone.game.behaviour;

import net.demilich.metastone.game.GameContext;

import java.util.Random;

public class PlayGameLogicRandomBehaviour extends PlayRandomBehaviour {

	@Override
	public String getName() {
		return "Play Random Reproducibly";
	}

	@Override
	protected Random getRandom(GameContext context) {
		return context.getLogic().getRandom();
	}
}
