package net.demilich.metastone.game.decks;

import com.google.common.base.Objects;
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
	public boolean isInFormat(String set) {
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FixedCardsDeckFormat)) return false;
		FixedCardsDeckFormat that = (FixedCardsDeckFormat) o;
		return Objects.equal(cardIds, that.cardIds);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(cardIds);
	}
}
