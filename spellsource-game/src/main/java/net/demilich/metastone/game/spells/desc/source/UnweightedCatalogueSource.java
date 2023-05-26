package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns a list of collectible cards from the {@link net.demilich.metastone.game.decks.DeckFormat} for this game (from
 * {@link GameContext#getDeckFormat()}) of equal probability for all classes and neutrals.
 * <p>
 * This is the default card source for random generation effects in all spells except {@link
 * net.demilich.metastone.game.spells.DiscoverSpell}.
 *
 * @see CatalogueSource for an example of a card source that returns the caster's class cards 4x more frequently than
 * 		neutrals, and never returns other class cards.
 */
public final class UnweightedCatalogueSource extends CardSource implements HasCardCreationSideEffects {

	public UnweightedCatalogueSource(CardSourceDesc desc) {
		super(desc);
	}

	public static UnweightedCatalogueSource create() {
		return new UnweightedCatalogueSource(new CardSourceDesc(UnweightedCatalogueSource.class));
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return context.getCardCatalogue().query(context.getDeckFormat());
	}
}
