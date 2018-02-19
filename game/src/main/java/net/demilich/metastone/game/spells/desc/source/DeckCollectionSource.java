package net.demilich.metastone.game.spells.desc.source;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.Deck;

import java.io.Serializable;

public class DeckCollectionSource extends CardSource implements Serializable, HasCardCreationSideEffects {

	public DeckCollectionSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	protected CardList match(GameContext context, Player player) {
		final String collectionName = desc.getString(SourceArg.COLLECTION_NAME);
		final Deck deck = context.getDeck(player, collectionName);
		if (deck != null) {
			return deck.getCards();
		}
		return null;
	}
}
