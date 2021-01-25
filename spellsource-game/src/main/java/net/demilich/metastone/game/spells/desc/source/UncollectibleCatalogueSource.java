package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns all cards, including uncollectible ones, in the game context's {@link net.demilich.metastone.game.decks.DeckFormat}.
 */
public class UncollectibleCatalogueSource extends CardSource implements HasCardCreationSideEffects {

	public UncollectibleCatalogueSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return CardCatalogue.query(context.getDeckFormat(), always -> true);
	}

}

