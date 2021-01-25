package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

/**
 * {@code true} if there are at least {@link ConditionArg#VALUE} of the {@code player}'s minions that satisfy the {@link
 * ConditionArg#CARD_FILTER}
 */
public class MinionOnBoardCondition extends Condition {

	public MinionOnBoardCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var cardFilter = (EntityFilter) desc.get(ConditionArg.CARD_FILTER);
		var value = desc.containsKey(ConditionArg.VALUE) ? desc.getInt(ConditionArg.VALUE) : 1;

		var count = 0;
		for (var minion : player.getMinions()) {
			if ((cardFilter == null || cardFilter.matches(context, player, minion, source)) && !context.getSummonReferenceStack().contains(minion.getReference())) {
				count++;
			}
		}

		return count >= value;
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
