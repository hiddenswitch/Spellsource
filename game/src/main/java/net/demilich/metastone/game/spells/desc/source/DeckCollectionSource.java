package net.demilich.metastone.game.spells.desc.source;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.Entity;

import java.io.Serializable;

import static java.util.stream.Collectors.toMap;

public class DeckCollectionSource extends CardSource implements Serializable, HasCardCreationSideEffects, HasWeights {

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
