package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityEqualsFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Evalutes to {@code true} if all the {@link ConditionArg#TARGET} entities pass the {@link ConditionArg#FILTER}.
 *
 * @see AnyMatchFilterCondition for a version of this condition where <b>any</b> matching filter causes the condition to
 * evaluate to {@code true}.
 */
public class AllMatchFilterCondition extends Condition {

	public AllMatchFilterCondition(ConditionDesc desc) {
		super(desc);
	}

	/**
	 * Creates a condition where the {@code lhs} entity reference. must be the same as the {@code rhs} entity reference.
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public static Condition create(EntityReference lhs, EntityReference rhs) {
		ConditionDesc desc = new ConditionDesc(AllMatchFilterCondition.class);
		desc.put(ConditionArg.TARGET, lhs);
		desc.put(ConditionArg.FILTER, EntityEqualsFilter.create(rhs));
		return desc.create();
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		EntityReference targetReference = (EntityReference) desc.get(ConditionArg.TARGET);
		EntityFilter filter = (EntityFilter) desc.get(ConditionArg.FILTER);
		if (targetReference == null && target != null) {
			targetReference = target.getReference();
		}

		boolean allMatch = true;
		for (Entity entity : context.resolveTarget(player, source, targetReference)) {
			allMatch &= filter == null || filter.matches(context, player, entity, source);
		}

		return allMatch;
	}
}
