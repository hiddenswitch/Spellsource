package net.demilich.metastone.game.spells.desc.valueprovider;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CardSourceDesc;
import net.demilich.metastone.game.spells.desc.source.HandSource;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Returns the number of cards in the {@link ValueProviderArg#CARD_SOURCE} and {@link ValueProviderArg#CARD_FILTER} when
 * specified, or {@link net.demilich.metastone.game.targeting.Zones#HAND} when not specified.
 */
public class CardCountValueProvider extends ValueProvider {

	public CardCountValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	protected int provideValue(GameContext context, Player player, Entity target, Entity source) {
		CardSource cardSource = (CardSource) getDesc().get(ValueProviderArg.CARD_SOURCE);
		if (cardSource == null) {
			cardSource = new CardSourceDesc(HandSource.class).create();
		}

		EntityFilter cardFilter;
		if (getDesc().containsKey(ValueProviderArg.CARD_FILTER)) {
			cardFilter = (EntityFilter) getDesc().get(ValueProviderArg.CARD_FILTER);
		} else {
			cardFilter = AndFilter.create();
		}

		if (getDesc().containsKey(ValueProviderArg.ATTRIBUTE)) {
			Attribute atr = (Attribute) getDesc().get(ValueProviderArg.ATTRIBUTE);
			return cardSource.getCards(context, source, player)
					.filtered(cardFilter.matcher(context, player, source))
					.stream()
					.mapToInt(c -> c.getAttributeValue(atr))
					.sum();
		} else return cardSource.getCards(context, source, player)
				.filtered(cardFilter.matcher(context, player, source))
				.getCount();
	}

}
