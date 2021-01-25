package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code true} if the {@code target} or the single entity resolved by {@link ConditionArg#TARGET} is damaged.
 */
public class IsDamagedCondition extends Condition {

	private static Logger LOGGER = LoggerFactory.getLogger(IsDamagedCondition.class);

	public IsDamagedCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		if (!(target instanceof Actor)) {
			LOGGER.warn("isFulfilled {} {}: target {} is not actor", context.getGameId(), desc, target);
			return false;
		}
		return ((Actor) target).isWounded();
	}

	@Override
	protected boolean usesFilter() {
		return false;
	}
}

