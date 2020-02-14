package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.cards.Card;

import java.util.Objects;
import java.util.Set;

public final class FixedCardsDeckFormat extends DeckFormat {
	private final Set<String> cardIds;

	public FixedCardsDeckFormat(String... cardIds) {
		super();
		this.cardIds = Set.of(cardIds);
	}

	@Override
	public boolean isInFormat(Card card) {
		return cardIds.contains(card.getCardId());
	}

	@Override
	public boolean isInFormat(String set) {
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FixedCardsDeckFormat)) return false;
		FixedCardsDeckFormat that = (FixedCardsDeckFormat) o;
		return Objects.equals(cardIds, that.cardIds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(cardIds);
	}
}
