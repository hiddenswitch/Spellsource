package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.TargetLogic;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

import java.util.List;

/**
 * {@code true} if the minion count with the specified {@link ConditionArg#FILTER} or {@link ConditionArg#CARD_FILTER}
 * evaluates to true with the specified {@link ConditionArg#OPERATION} and {@link ConditionArg#VALUE}.
 */
public class MinionCountCondition extends CountCondition {

	public MinionCountCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected int getCountForPlayer(GameContext context, Player player, Entity source, Entity target) {
		List<Minion> minions = TargetLogic.withoutPermanents(player.getMinions());
		int count = 0;
		EntityFilter filter = (EntityFilter) getDesc().get(ConditionArg.FILTER);
		if (filter == null) {
			filter = (EntityFilter) getDesc().get(ConditionArg.CARD_FILTER);
		}
		for (Entity minion : minions) {
			if (filter == null || filter.matches(context, player, source, minion)) {
				count++;
			}
		}
		return count;
	}
}

