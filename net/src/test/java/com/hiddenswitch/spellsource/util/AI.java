package com.hiddenswitch.spellsource.util;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;

import java.io.Serializable;

public class AI extends PlayRandomBehaviour implements Serializable {

	public AI() {
		super();
	}

	@Override
	public void onGameOver(GameContext context, int playerId, int winningPlayerId) {
	}
}
