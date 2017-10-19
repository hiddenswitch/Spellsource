package net.demilich.metastone.game.spells.desc.condition;

import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

public class PlayedLastTurnCondition extends Condition {

	public PlayedLastTurnCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		Map<String, Map<Integer, Integer>> cardIds = player.getStatistics().getCardsPlayed();
		int count = 0;
		int turn = context.getTurn();
		if (player.getId() == context.getActivePlayerId()) {
			turn -= 2;
		} else {
			turn -= 1;
		}
		EntityFilter filter = (EntityFilter) desc.get(ConditionArg.FILTER);
		for (String cardId : cardIds.keySet()) {
			Entity entity = context.getCardById(cardId);
			if (filter == null || filter.matches(context, player, entity, source)) {
				if (cardIds.get(cardId).containsKey(turn)) {
					count += cardIds.get(cardId).get(turn);
				}
			}
		}
		return count >= 1;
	}

}
