package net.demilich.metastone.tests.util;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.fibers.SuspendableGameContext;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.cards.Attribute;

public class DebugContext extends GameContext {

	public DebugContext(Player player1, Player player2, GameLogic logic, DeckFormat deckFormat) {
		super();
		setPlayer(0,player1);
		setPlayer(1,player2);
		setLogic(logic);
		setDeckFormat(deckFormat);
	}

	@Override
	@Suspendable
	public void init() {
		super.init();
		startTrace();
		startTurn(getActivePlayerId());
	}

	@Suspendable
	public void endTurn() {
		super.endTurn();
		startTurn(getActivePlayerId());
	}
}