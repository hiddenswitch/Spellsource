package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns the top three cards of the deck.
 * <p>
 * Implements Tracking.
 */
public final class TopThreeCardsOfDeckSource extends TopCardsOfDeckSource {

	public TopThreeCardsOfDeckSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return super.match(context, source, player, 3);
	}
}
