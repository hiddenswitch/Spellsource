package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.AttributeFilter;

import static java.util.stream.Collectors.toCollection;

/**
 * Returns a list of cards that are in the player's graveyard.
 * <p>
 * This includes the cards the user has played, discarded or roasted. To differentiate between them, use an {@link
 * AttributeFilter} for the attributes {@link Attribute#PLAYED_FROM_HAND_OR_DECK}, {@link Attribute#DISCARDED} or {@link
 * Attribute#ROASTED}.
 */
public class GraveyardCardsSource extends CardSource {

	public GraveyardCardsSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return player
				.getGraveyard()
				.stream()
				.filter(e -> e instanceof Card)
				.map(e -> (Card) e)
				.collect(toCollection(CardArrayList::new));
	}
}

