package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

/**
 * Evaluates to {@code true} if the {@link EntityReference#EVENT_TARGET} equals the {@link ConditionArg#TARGET} list of
 * entities.
 * <p>
 * Note that {@link ConditionArg#TARGET} will not be resolved element-by-element here.
 */
public class EventTargetMatchesReferenceCondition extends EntityEqualityCondition {

	public EventTargetMatchesReferenceCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected List<Entity> lhs(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			EntityReference targetReference,
			EntityReference secondaryTarget) {
		return context.resolveTarget(player, source, targetReference);
	}

	@Override
	protected List<Entity> rhs(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			EntityReference targetReference,
			EntityReference secondaryTarget) {
		return context.resolveTarget(player, source, EntityReference.EVENT_TARGET);
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
