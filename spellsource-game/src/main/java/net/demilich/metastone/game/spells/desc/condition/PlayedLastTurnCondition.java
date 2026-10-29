package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;


/**
 * {@code true} if cards filtered by {@link ConditionArg#FILTER} were played by the {@link ConditionArg#TARGET_PLAYER}
 * last turn.
 */
public class PlayedLastTurnCondition extends Condition {

	public PlayedLastTurnCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			EntityFilter filter) {
		var cardIds = player.getStatistics().getCardsPlayed();
		var count = 0;
		var turn = player.getAttributeValue(Attribute.LAST_TURN, -1);
		if (turn == -1) {
			return false;
		}
		for (var cardId : cardIds.keySet()) {
			Entity entity = context.getCardById(cardId);
			if (filter == null || filter.matches(context, player, entity, source)) {
				if (cardIds.get(cardId).containsKey(turn)) {
					count += cardIds.get(cardId).get(turn);
				}
			}
		}
		return count >= 1;
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}
}
