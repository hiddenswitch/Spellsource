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

	protected boolean isFulfilled(GameContext context, Player player, Entity source, Entity target, EntityFilter cardFilter) {
		var hand = player.getHand();
		for (int i = 0, n = hand.size(); i < n; i++) {
			var card = hand.get(i);
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
