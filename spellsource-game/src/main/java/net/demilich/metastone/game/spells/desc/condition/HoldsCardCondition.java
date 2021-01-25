package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code true} if the player is holding a card matching the {@link ConditionArg#CARD_FILTER} and their hand is not
 * empty.
 */
public class HoldsCardCondition extends Condition {

	private static Logger LOGGER = LoggerFactory.getLogger(HoldsCardCondition.class);

	public HoldsCardCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		if (desc.containsKey(ConditionArg.FILTER)) {
			LOGGER.warn("isFulfilled {} {}: Did you mean CARD_FILTER?", context.getGameId(), source);
		}

		var cardFilter = (EntityFilter) desc.get(ConditionArg.CARD_FILTER);
		for (var card : player.getHand()) {
			if (cardFilter == null || cardFilter.matches(context, player, card, source)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
