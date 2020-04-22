package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

/**
 * Counts the number of cards in the player's hand, and evalutes to {@code true} if the count equals the {@link
 * ConditionArg#VALUE}.
 * <p>
 * {@link ConditionArg#FILTER} is used to filter the cards counted; or, if not specified, {@link
 * ConditionArg#CARD_FILTER} is used; or the total hand count is used.
 */
public class CardCountCondition extends CountCondition {

	public CardCountCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected int getCountForPlayer(GameContext context, Player player, Entity source, Entity target) {
		int count = 0;
		EntityFilter filter = (EntityFilter) getDesc().get(ConditionArg.FILTER);
		if (filter == null) {
			filter = (EntityFilter) getDesc().get(ConditionArg.CARD_FILTER);
		}
		for (Entity card : player.getHand()) {
			if (filter == null || filter.matches(context, player, source, card)) {
				count++;
			}
		}
		return count;
	}
}

