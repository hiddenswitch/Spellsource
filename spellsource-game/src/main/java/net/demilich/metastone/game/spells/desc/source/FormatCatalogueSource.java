package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Default;

/**
 * Returns a list of collectible cards from the {@link net.demilich.metastone.game.decks.DeckFormat} specified in {@link
 * CardSourceArg#FORMAT} where class cards of the casting player's class appear 4x more than neutrals, and no other
 * class cards appear.
 */
public class FormatCatalogueSource extends CardSource implements HasCardCreationSideEffects {

	public FormatCatalogueSource(CardSourceDesc desc) {
		super(desc);
	}

	protected CardList match(GameContext context, Entity source, Player player, @Default String format) {
		return context.getCardCatalogue().query(context.getCardCatalogue().getFormat(format), null, null, null, null, false);
	}
}
