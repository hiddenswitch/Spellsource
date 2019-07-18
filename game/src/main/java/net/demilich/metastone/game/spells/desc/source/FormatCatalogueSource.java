package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns a list of cards in the specified {@link CardSourceArg#FORMAT}.
 *
 * @see DeckFormat for the formats.
 */
public class FormatCatalogueSource extends CardSource implements HasCardCreationSideEffects {

	public FormatCatalogueSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return CardCatalogue.query(DeckFormat.getFormat(getDesc().getString(CardSourceArg.FORMAT)));
	}
}
