package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Returns the number of cards matching {@link ValueProviderArg#FILTER} or {@link ValueProviderArg#CARD_FILTER} that
 * were played this turn by the {@link ValueProviderArg#TARGET_PLAYER}.
 *
 * @see CardsPlayedValueProvider for all cards played by the player this game.
 */
public class PlayedThisTurnValueProvider extends ValueProvider {

	private static Logger LOGGER = LoggerFactory.getLogger(PlayedThisTurnValueProvider.class);

	public PlayedThisTurnValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		Map<String, Map<Integer, Integer>> cardIds = player.getStatistics().getCardsPlayed();
		EntityFilter filter = (EntityFilter) getDesc().get(ValueProviderArg.FILTER);
		if (filter == null) {
			filter = (EntityFilter) getDesc().get(ValueProviderArg.CARD_FILTER);
		}
		if (filter == null) {
			filter = AndFilter.create();
		}

		int value = 0;
		for (Map.Entry<String, Map<Integer, Integer>> kv : cardIds.entrySet()) {
			String cardId = kv.getKey();
			Card card = context.getCardById(cardId);
			if (filter.matches(context, player, card, host)) {
				value += kv.getValue().getOrDefault(context.getTurn(), 0);
			}
		}
		return value;
	}
}

