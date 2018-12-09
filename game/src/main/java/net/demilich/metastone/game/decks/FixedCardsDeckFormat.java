package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class FixedCardsDeckFormat extends DeckFormat {
	private final Set<String> cardIds;

	public FixedCardsDeckFormat(String... cardIds) {
		super();
		this.cardIds = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(cardIds)));
	}

	@Override
	public boolean isInFormat(Card card) {
		return cardIds.contains(card.getCardId());
	}

	@Override
	public boolean isInFormat(CardSet set) {
		return false;
	}
}
