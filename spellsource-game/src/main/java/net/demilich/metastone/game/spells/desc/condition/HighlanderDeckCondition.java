package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Evaluates to {@code true} when there is exactly one copy of each card in the {@link EntityReference#FRIENDLY_DECK}.
 */
public class HighlanderDeckCondition extends NumberOfCopiesCondition {

	public HighlanderDeckCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		desc.put(ConditionArg.TARGET, EntityReference.FRIENDLY_DECK);
		desc.put(ConditionArg.OPERATION, ComparisonOperation.EQUAL);
		desc.put(ConditionArg.VALUE, 1);
		return super.isFulfilled(context, player, desc, source, target);
	}
}

