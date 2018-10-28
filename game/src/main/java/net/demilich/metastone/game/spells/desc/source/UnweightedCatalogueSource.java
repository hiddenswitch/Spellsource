package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

import java.util.Map;

public class UnweightedCatalogueSource extends CardSource implements HasCardCreationSideEffects {

	public UnweightedCatalogueSource(CardSourceDesc desc) {
		super(desc);
	}

	public static UnweightedCatalogueSource create() {
		return new UnweightedCatalogueSource(new CardSourceDesc(UnweightedCatalogueSource.class));
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return CardCatalogue.query(context.getDeckFormat());
	}
}
