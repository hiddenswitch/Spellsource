package net.demilich.metastone.game.spells.desc.valueprovider;

import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

/**
 * Counts the number of cards played of {@link ValueProviderArg#FILTER} by {@link ValueProviderArg#TARGET_PLAYER} across
 * the whole game.
 */
public class CardsPlayedValueProvider extends ValueProvider {

	public CardsPlayedValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(GameContext context, Player player, Entity target, Entity host, EntityFilter filter) {
		Map<String, Map<Integer, Integer>> cardIds = player.getStatistics().getCardsPlayed();
		int count = 0;
		for (String cardId : cardIds.keySet()) {
			Card card = context.getCardById(cardId);
			if (filter == null || filter.matches(context, player, card, host)) {
				for (Integer turn : cardIds.get(cardId).keySet()) {
					count += cardIds.get(cardId).get(turn);
				}
			}
		}
		return count;
	}

}
