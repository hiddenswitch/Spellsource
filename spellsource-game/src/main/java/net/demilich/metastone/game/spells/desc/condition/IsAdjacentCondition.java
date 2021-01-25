package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * {@code true} if the {@code source} is adjacent to the {@code target} or, when specified, the {@link
 * ConditionArg#TARGET}.
 * <p>
 * When being evaluated within an event stack, {@code source} is overridden to the {@link
 * EntityReference#TRIGGER_HOST}.
 */
public class IsAdjacentCondition extends Condition {

	public IsAdjacentCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		Entity hasTriggerHost = context.resolveSingleTarget(player, source, EntityReference.TRIGGER_HOST);

		if (hasTriggerHost != null) {
			source = hasTriggerHost;
		}

		for (Actor minion : context.getAdjacentMinions(source.getReference())) {
			if (minion.getId() == target.getId()) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}
}
