package net.demilich.metastone.game.spells.desc.valueprovider;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.HandSource;
import net.demilich.metastone.game.spells.desc.source.SourceDesc;

public class CardCountValueProvider extends ValueProvider {

	public CardCountValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	protected int provideValue(GameContext context, Player player, Entity target, Entity source) {
		CardSource cardSource = (CardSource) desc.get(ValueProviderArg.CARD_SOURCE);
		if (cardSource == null) {
			cardSource = new HandSource(new SourceDesc(SourceDesc.build(HandSource.class)));
		}

		EntityFilter cardFilter;
		if (desc.containsKey(ValueProviderArg.CARD_FILTER)) {
			cardFilter = (EntityFilter) desc.get(ValueProviderArg.CARD_FILTER);
		} else {
			cardFilter = AndFilter.create();
		}

		return cardSource.getCards(context, source, player)
				.filtered(c -> cardFilter.matches(context, player, c, source))
				.getCount();
	}

}
