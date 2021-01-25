package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

import java.util.Map;

public class PlayedLastTurnValueProvider extends ValueProvider {

	public PlayedLastTurnValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		Map<String, Map<Integer, Integer>> cardIds = player.getStatistics().getCardsPlayed();
		int count = 0;
		int turn = context.getTurn();
		if (player.getId() == context.getActivePlayerId()) {
			turn -= 2;
		} else {
			turn -= 1;
		}
		EntityFilter filter = (EntityFilter) getDesc().get(ValueProviderArg.FILTER);
		for (String cardId : cardIds.keySet()) {
			Entity entity = context.getCardById(cardId);
			if (filter == null || filter.matches(context, player, entity, host)) {
				if (cardIds.get(cardId).containsKey(turn)) {
					count += cardIds.get(cardId).get(turn);
				}
			}
		}
		return count;
	}

}

