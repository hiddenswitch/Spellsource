package com.hiddenswitch.spellsource.util;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.cards.Attribute;

public class DebugContext extends GameContext {

	public DebugContext(Player player1, Player player2, GameLogic logic, DeckFormat deckFormat) {
		super();
		setPlayer(0, player1);
		setPlayer(1, player2);
		setLogic(logic);
		setDeckFormat(deckFormat);
	}

	@Override
	public void init() {
		super.init(0);
		startTurn(0);
	}

	public void endTurn() {
		super.endTurn();
		startTurn(getActivePlayerId());
	}

	public void forceStartTurn(int playerId) {
		startTurn(playerId);
	}

}
