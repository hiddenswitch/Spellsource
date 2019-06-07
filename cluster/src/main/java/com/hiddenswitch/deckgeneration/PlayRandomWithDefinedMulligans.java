package com.hiddenswitch.deckgeneration;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;

import java.util.ArrayList;
import java.util.List;

public class PlayRandomWithDefinedMulligans extends PlayRandomWithoutSelfDamageBehaviour {
	List<String> cardsToKeep;

	public PlayRandomWithDefinedMulligans(List<String> cardsToKeep) {
		this.cardsToKeep = cardsToKeep;
	}

	@Override
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		List<Card> discardedCards = new ArrayList<Card>();
		for (Card card : cards) {
			if (!cardsToKeep.contains(card.getCardId())) {
				discardedCards.add(card);
			}
		}
		return discardedCards;
	}
}
