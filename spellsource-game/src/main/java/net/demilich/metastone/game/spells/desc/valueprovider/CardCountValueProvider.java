package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CardSourceDesc;
import net.demilich.metastone.game.spells.desc.source.HandSource;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Returns the number of cards in the {@link ValueProviderArg#CARD_SOURCE} and {@link ValueProviderArg#CARD_FILTER} when
 * specified, or {@link com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones#HAND} when not specified.
 * <p>
 * If an {@link ValueProviderArg#ATTRIBUTE} is specified, sums the values stored in the attribute or returns the count
 * of cards with that attribute.
 */
public class CardCountValueProvider extends EntityCountValueProvider {

	public CardCountValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(
			GameContext context,
			Player player,
			Entity target,
			Entity source,
			CardSource cardSource,
			EntityFilter cardFilter,
			Attribute attribute) {
		if (cardSource == null) {
			cardSource = HandSource.create();
		}

		if (cardFilter == null) {
			cardFilter = AndFilter.create();
		}

		CardList cards = cardSource.getCards(context, source, player)
				.filtered(cardFilter.matcher(context, player, source));
		if (attribute != null) {
			return cards
					.stream()
					.mapToInt(card -> card.getAttributeValue(attribute))
					.sum();
		} else {
			return cards
					.getCount();
		}
	}

}
