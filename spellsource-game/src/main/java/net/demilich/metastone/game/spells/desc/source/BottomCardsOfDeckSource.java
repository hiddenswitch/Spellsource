package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns the bottom N cards of the deck, where N defaults to 3.
 * <p>
 * Implements the Dredge keyword from Hearthstone's Voyage to the Sunken City.
 */
public class BottomCardsOfDeckSource extends DeckSource {

	public BottomCardsOfDeckSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		CardList deck = super.match(context, source, player);
		int value = getDesc().getValue(CardSourceArg.VALUE, context, player, player, source, 3);
		int count = Math.min(value, deck.size());
		return new CardArrayList(deck.subList(0, count));
	}
}
