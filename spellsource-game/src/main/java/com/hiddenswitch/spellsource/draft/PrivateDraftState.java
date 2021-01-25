package com.hiddenswitch.spellsource.draft;

import net.demilich.metastone.game.logic.XORShiftRandom;

import java.io.Serializable;
import java.util.List;

/**
 * Private information about the player's draft. This includes the actual list of cards the player will see, so it
 * should not be shared with the client.
 */
public final class PrivateDraftState implements Serializable {
	private List<List<String>> cards;
	private XORShiftRandom random = new XORShiftRandom(XORShiftRandom.createSeed());

	public PrivateDraftState() {
	}

	/**
	 * The {@link DraftLogic#ROUNDS} worth of {@link DraftLogic#CARDS_PER_ROUND} card choices this player will have.
	 *
	 * @return A list of cards
	 */
	public List<List<String>> getCards() {
		return cards;
	}

	public void setCards(List<List<String>> cards) {
		this.cards = cards;
	}

	/**
	 * Returns the random instance used for this draft.
	 *
	 * @return
	 */
	public XORShiftRandom getRandom() {
		return random;
	}

	public PrivateDraftState setRandom(XORShiftRandom random) {
		this.random = random;
		return this;
	}
}
