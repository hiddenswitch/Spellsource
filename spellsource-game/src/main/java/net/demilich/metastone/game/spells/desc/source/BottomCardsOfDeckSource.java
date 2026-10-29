package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Default;

import static java.util.stream.Collectors.toCollection;

/**
 * Returns the bottom {@link CardSourceArg#VALUE} cards of the {@code player}'s deck.
 */
public class BottomCardsOfDeckSource extends DeckSource {

	public BottomCardsOfDeckSource(CardSourceDesc desc) {
		super(desc);
	}

	protected CardList match(GameContext context, Entity source, Player player, @Default(3) int value) {
		CardList deck = super.match(context, source, player);
		return deck
				.stream()
				.skip(Math.max(deck.size() - value, 0))
				.collect(toCollection(CardArrayList::new));
	}
}
