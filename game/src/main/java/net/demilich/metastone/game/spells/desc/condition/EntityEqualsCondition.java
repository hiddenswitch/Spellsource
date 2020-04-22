package net.demilich.metastone.game.spells.desc.condition;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Collections;
import java.util.List;

/**
 * Evaluates to {@code true} when the {@code target} or {@link ConditionArg#TARGET} and {@link ConditionArg#SECONDARY_TARGET}
 * are the same.
 * <p>
 * Does not support multiple targets on either side.
 */
public class EntityEqualsCondition extends EntityEqualityCondition {

	public EntityEqualsCondition(ConditionDesc desc) {
		super(desc);
	}

	public static ConditionDesc create(EntityReference lhs, EntityReference rhs) {
		var desc = new ConditionDesc(EntityEqualsCondition.class);
		desc.put(ConditionArg.SECONDARY_TARGET,rhs);
		desc.put(ConditionArg.TARGET,lhs);
		return desc;
	}

	@Override
	protected List<Entity> rhs(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var entity = context.resolveSingleTarget(player, source, (EntityReference) desc.get(ConditionArg.SECONDARY_TARGET));
		if (entity == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(entity);
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}
}
