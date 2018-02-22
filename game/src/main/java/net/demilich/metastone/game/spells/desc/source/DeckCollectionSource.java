package net.demilich.metastone.game.spells.desc.source;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.Entity;

import java.io.Serializable;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class DeckCollectionSource extends CardSource implements Serializable, HasCardCreationSideEffects, HasWeights {

	public DeckCollectionSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	protected CardList match(GameContext context, Entity source, Player player) {
		final String collectionName = desc.getString(SourceArg.COLLECTION_NAME);
		final Deck deck = context.getDeck(player, collectionName);
		if (deck != null) {
			return new CardArrayList(deck.getCards()
					.stream()
					.collect(toMap(Card::getCardId, Function.identity(), (p, q) -> p))
					.values());
		}
		return new CardArrayList();
	}
}
