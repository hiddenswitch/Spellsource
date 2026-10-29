package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

/**
 * Evaluates to {@code true} if the player's minion count filtered by {@link ConditionArg#FILTER} or {@link
 * ConditionArg#CARD_FILTER} satisfies the {@link ConditionArg#OPERATION} with {@link ConditionArg#VALUE}.
 * <p>
 * Note that this includes {@link net.demilich.metastone.game.entities.minions.Permanent} entities on the battlefield;
 * the class name is a misnomer.
 * <p>
 * Does not use the {@link ConditionArg#TARGET} parameter.
 */
public class MinionCountCondition extends CountCondition {

	public MinionCountCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected int getCountForPlayer(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			EntityFilter filter,
			EntityFilter cardFilter) {
		EntityFilter activeFilter = filter != null ? filter : cardFilter;
		return (int) player.getMinions().stream()
				.filter(activeFilter != null ? activeFilter.matcher(context, player, source) : m -> true)
				.count();
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
