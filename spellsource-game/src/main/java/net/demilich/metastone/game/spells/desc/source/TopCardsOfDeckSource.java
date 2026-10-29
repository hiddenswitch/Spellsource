package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Default;

import static java.util.stream.Collectors.toCollection;

/**
 * Returns the top {@link CardSourceArg#VALUE} cards from the deck.
 */
public class TopCardsOfDeckSource extends DeckSource {

	public TopCardsOfDeckSource(CardSourceDesc desc) {
		super(desc);
	}

	protected CardList match(GameContext context, Entity source, Player player, @Default(3) int value) {
		return super.match(context, source, player)
				.stream()
				.limit(value)
				.collect(toCollection(CardArrayList::new));
	}
}
