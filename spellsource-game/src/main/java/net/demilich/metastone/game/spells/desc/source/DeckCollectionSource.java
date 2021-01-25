package net.demilich.metastone.game.spells.desc.source;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.Entity;

import java.io.Serializable;

/**
 * Returns the list of cards from the deck in the user's collection named {@link CardSourceArg#COLLECTION_NAME}. The
 * name is case insensitive, and the first deck found is used.
 * <p>
 * Uses the {@link GameContext#getDeck(Player, String)} method, which provides an implementation in server game
 * contexts.
 */
public class DeckCollectionSource extends CardSource implements Serializable, HasCardCreationSideEffects {

	public DeckCollectionSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	protected CardList match(GameContext context, Entity source, Player player) {
		final String collectionName = getDesc().getString(CardSourceArg.COLLECTION_NAME);
		final GameDeck deck = context.getDeck(player, collectionName);
		if (deck != null) {
			return deck.getCards();
		}
		return new CardArrayList();
	}
}
