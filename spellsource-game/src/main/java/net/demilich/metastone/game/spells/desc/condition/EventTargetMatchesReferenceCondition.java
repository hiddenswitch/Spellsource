package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

/**
 * Evaluates to {@code true} when the {@link EntityReference#EVENT_TARGET} is one of the entities referenced by
 * {@link ConditionArg#TARGET}, resolved from the perspective of the {@link EntityReference#TRIGGER_HOST}.
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
		var hosts = context.resolveTarget(player, source, EntityReference.TRIGGER_HOST);
		var perspective = hosts == null || hosts.isEmpty() || hosts.get(0) == null ? source : hosts.get(0);
		return context.resolveTarget(player, perspective, targetReference);
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
	protected boolean compare(List<Entity> lhs, List<Entity> rhs) {
		return lhs.containsAll(rhs);
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
