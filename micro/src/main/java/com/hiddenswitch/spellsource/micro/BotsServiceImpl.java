package com.hiddenswitch.spellsource.micro;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.logic.DeckTrace;
import net.demilich.metastone.game.logic.MulliganTrace;
import net.demilich.metastone.game.logic.Trace;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
class BotsServiceImpl implements BotsService {
	@Override
	public List<Integer> request(Payload payload) {
		Trace trace = new Trace();
		trace.setSeed(payload.getTrace().getSeed());
		trace.setCatalogueVersion(payload.getTrace().getCatalogueVersion());
		trace.setHeroClasses(payload.getTrace().getHeroClasses());
		trace.setDeckCardIds(payload.getTrace().getDeckCardIds().stream().map(s -> new DeckTrace().setCardIds(s.getCardIds()).setPlayerId(s.getPlayerId())).collect(Collectors.toList()));
		trace.setDeckFormatName(payload.getTrace().getDeckFormatName());
		trace.setDeckFormatSets(payload.getTrace().getDeckFormatSets());
		trace.setSecondPlayerBonusCards(payload.getTrace().getSecondPlayerBonusCards());
		trace.setMulligans(payload.getTrace().getMulligans().stream().map(s -> new MulliganTrace().setEntityIds(s.getEntityIds()).setPlayerId(s.getPlayerId())).collect(Collectors.toList()));
		trace.setActions(payload.getTrace().getActions());
		trace.setId(payload.getTrace().getId());
		trace.setTraceErrors(payload.getTrace().isTraceErrors());
		trace.setVersion(payload.getTrace().getVersion());
		GameContext gameContext = GameContext.fromTrace(trace);
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

