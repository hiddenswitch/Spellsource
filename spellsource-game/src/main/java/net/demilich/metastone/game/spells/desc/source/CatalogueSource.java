package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns a list of collectible cards from the  {@link net.demilich.metastone.game.decks.DeckFormat} for this game
 * (from {@link GameContext#getDeckFormat()}) where class cards of the casting player's class appear 4x more than
 * neutrals, and no other class cards appear.
 * <p>
 * This source has {@link HasCardCreationSideEffects}, which indicates that it doesn't refer to cards inside the game
 * but rather generates new copies on the fly.
 * <p>
 * This source also has the aforementioned weights indicated by {@link HasWeights}.
 * <p>
 * This is the default card source for {@link net.demilich.metastone.game.spells.DiscoverSpell}.
 *
 * @see UnweightedCatalogueSource for a card source that returns all cards in the deck format from all classes as
 * 		frequently as neutrals.
 */
public class CatalogueSource extends CardSource implements HasCardCreationSideEffects, HasWeights {

	private static CatalogueSource INSTANCE = new CatalogueSource(new CardSourceDesc(CatalogueSource.class));

	public static CatalogueSource create() {
		return INSTANCE;
	}

	public CatalogueSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return context.getCardCatalogue().query(context.getDeckFormat());
	}

}

