package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;

import java.util.Map;

public class CatalogueSource extends CardSource implements HasCardCreationSideEffects, HasWeights {

	public static CatalogueSource create() {
		Map<SourceArg, Object> args = SourceDesc.build(CatalogueSource.class);
		return new CatalogueSource(new SourceDesc(args));
	}

	public CatalogueSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Player player) {
		return CardCatalogue.query(context.getDeckFormat());
	}

}

