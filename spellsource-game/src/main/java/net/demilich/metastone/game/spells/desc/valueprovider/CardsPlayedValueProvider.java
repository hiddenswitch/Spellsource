package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

import java.util.Map;

/**
 * Returns the count of the number of cards played that by the {@link ValueProviderArg#TARGET_PLAYER} that satisfies the
 * specified {@link ValueProviderArg#FILTER}.
 * <p>
 * The filter is run against the base cards that were played, not the cards that were actually played.
 */
public class CardsPlayedValueProvider extends ValueProvider {

	public CardsPlayedValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		Map<String, Map<Integer, Integer>> cardIds = player.getStatistics().getCardsPlayed();
		int count = 0;
		EntityFilter filter = (EntityFilter) getDesc().get(ValueProviderArg.FILTER);
		for (String cardId : cardIds.keySet()) {
			Entity entity = context.getCardById(cardId);
			if (filter == null || filter.matches(context, player, entity, host)) {
				for (Integer turn : cardIds.get(cardId).keySet()) {
					count += cardIds.get(cardId).get(turn);
				}
			}
		}
		return count;
	}
}

