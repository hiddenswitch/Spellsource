package net.demilich.metastone.tests.util;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.logic.GameLogic;

public class DebugContext extends GameContext {

	public DebugContext(Player player1, Player player2, GameLogic logic, DeckFormat deckFormat) {
		this(player1, player2, logic);
		setDeckFormat(deckFormat);
	}

	public DebugContext(Player player1, Player player2, GameLogic logic) {
		super();
		setPlayer(0, player1);
		setPlayer(1, player2);
		setLogic(logic);
	}

	@Override
	public void init() {
		super.init();
		startTurn(getActivePlayerId());
	}

	public void endTurn() {
		super.endTurn();
		startTurn(getActivePlayerId());
	}
}