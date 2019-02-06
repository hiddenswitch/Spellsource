package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;

/**
 * Returns the index of the first entity that matches the {@link ValueProviderArg#CARD_FILTER} in the specified {@link
 * ValueProviderArg#CARD_SOURCE}.
 */
public class IndexInSourceValueProvider extends ValueProvider {
	public IndexInSourceValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		CardSource source = (CardSource) getDesc().get(ValueProviderArg.CARD_SOURCE);
		CardFilter filter = (CardFilter) getDesc().get(ValueProviderArg.CARD_FILTER);
		int i = 0;
		for (Card card : source.getCards(context, host, player)) {
			if (filter != null && !filter.matches(context, player, card, host)) {
				i++;
			} else return i;
		}

		return -1;
	}
}
