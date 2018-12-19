package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns the top three cards of the deck.
 * <p>
 * Implements Tracking.
 */
public final class TopThreeCardsOfDeckSource extends DeckSource {

	public TopThreeCardsOfDeckSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		CardList deck = super.match(context, source, player);
		return new CardArrayList(deck.subList(Math.max(0, deck.size() - 3), deck.size()));
	}
}
