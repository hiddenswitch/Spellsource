package net.demilich.metastone.tests.util;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.cards.Attribute;

public class DebugContext extends GameContext {

	public DebugContext(Player player1, Player player2, GameLogic logic, DeckFormat deckFormat) {
		super(player1, player2, logic, deckFormat);
	}

	@Override
	public void init() {
		super.init(0);
		startTurn(getActivePlayerId());
	}

	public void endTurn() {
		super.endTurn();
		startTurn(getActivePlayerId());
	}

}
