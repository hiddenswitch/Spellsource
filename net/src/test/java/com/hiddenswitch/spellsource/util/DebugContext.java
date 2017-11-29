package com.hiddenswitch.spellsource.util;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.utils.Attribute;

public class DebugContext extends GameContext {

	public DebugContext(Player player1, Player player2, GameLogic logic, DeckFormat deckFormat) {
		super(player1, player2, logic, deckFormat);
	}

	@Override
	public void init() {
		getPlayers().forEach(p -> p.getAttributes().put(Attribute.GAME_START_TIME_MILLIS, (int) (System.currentTimeMillis() % Integer.MAX_VALUE)));
		setActivePlayerId(getPlayer(PLAYER_1).getId());
		getLogic().initializePlayer(PLAYER_1);
		getLogic().initializePlayer(PLAYER_2);
		getLogic().init(getActivePlayerId(), true);
		getLogic().init(getOpponent(getActivePlayer()).getId(), false);
	}

	public void endTurn() {
		super.endTurn();
		startTurn(getActivePlayerId());
	}

	public void forceStartTurn(int playerId) {
		startTurn(playerId);
	}

}
