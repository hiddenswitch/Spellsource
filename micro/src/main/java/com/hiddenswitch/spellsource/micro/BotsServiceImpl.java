package com.hiddenswitch.spellsource.micro;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
class BotsServiceImpl implements BotsService {
	@Override
	public List<Integer> request(Payload payload) {
		GameContext gameContext = GameContext.fromTrace(payload.getTrace());
		GameStateValueBehaviour behaviour = new GameStateValueBehaviour()
				.setParallel(false)
				.setMaxDepth(5)
				.setTimeout(1000)
				.setLethalTimeout(10000);
		int playerId = payload.getPlayerId();
		if (playerId == -1) {
			playerId = gameContext.getActivePlayerId();
		}
		gameContext.setBehaviour(playerId, behaviour);
		var action = behaviour.requestAction(gameContext, gameContext.getPlayer(playerId), gameContext.getValidActions());
		ArrayList<Integer> actions = new ArrayList<>(behaviour.getIndexPlan());
		actions.add(0, action.getId());
		return actions;
	}
}

