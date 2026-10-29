package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CardSourceDesc;
import net.demilich.metastone.game.spells.desc.source.HandSource;

public class ManaCostModifierValueProvider extends ValueProvider {
	public ManaCostModifierValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(GameContext context, Player player, Entity target, Entity host, CardSource cardSource, EntityFilter cardFilter) {
		int value = 0;
		if (cardSource == null) {
			cardSource = new CardSourceDesc(HandSource.class).create();
		}

		if (cardFilter == null) {
			cardFilter = AndFilter.create();
		}

		CardList cards = cardSource.getCards(context, host, player).filtered(cardFilter.matcher(context, player, host));

		for (Card card : cards) {
			value += card.getManaCostModification(context, player);
		}

		return value;
	}
}
