package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

import java.util.Map;

public class CatalogueSource extends CardSource implements HasCardCreationSideEffects, HasWeights {

	public static CatalogueSource create() {
		Map<CardSourceArg, Object> args = new CardSourceDesc(CatalogueSource.class);
		return new CatalogueSource(new CardSourceDesc(args));
	}

	public CatalogueSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return CardCatalogue.query(context.getDeckFormat());
	}

}

