package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Evaluates to {@code true} if the player's deck has no duplicates (either at all or after being filtered by {@link
 * ConditionArg#FILTER}).
 */
public class HighlanderDeckCondition extends NumberOfCopiesCondition {

	private static final Condition INSTANCE = new HighlanderDeckCondition(new ConditionDesc(HighlanderDeckCondition.class));

	public static Condition create() {
		return INSTANCE;
	}

	public HighlanderDeckCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			EntityFilter filter) {
		return super.isFulfilled(context, player, source, target, EntityReference.FRIENDLY_DECK, filter, 1, ComparisonOperation.EQUAL);
	}
}
