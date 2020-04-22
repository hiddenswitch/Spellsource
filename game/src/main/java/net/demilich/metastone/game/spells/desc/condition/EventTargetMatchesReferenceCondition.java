package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluates to {@code true} when the {@link ConditionArg#TARGET} from the perspective of the {@link
 * EntityReference#TRIGGER_HOST} and the {@link EntityReference#EVENT_TARGET} are the same.
 */
public class EventTargetMatchesReferenceCondition extends EntityEqualityCondition {

	public EventTargetMatchesReferenceCondition(ConditionDesc desc) {
		super(desc);
	}


	@Override
	protected List<Entity> lhs(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return context.resolveTarget(player, context.resolveTarget(player, source, EntityReference.TRIGGER_HOST).get(0), (EntityReference) desc.get(ConditionArg.TARGET));
	}

	@Override
	protected List<Entity> rhs(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var entity = context.resolveSingleTarget(EntityReference.EVENT_TARGET);
		if (entity == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(entity);
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}