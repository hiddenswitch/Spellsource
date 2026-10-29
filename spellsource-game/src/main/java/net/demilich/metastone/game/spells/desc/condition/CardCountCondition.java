package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

/**
 * Evaluates to {@code true} if the player's hand card count filtered by {@link ConditionArg#FILTER} or {@link
 * ConditionArg#CARD_FILTER} satisfies the {@link ConditionArg#OPERATION} with {@link ConditionArg#VALUE}.
 * <p>
 * Does not use the {@link ConditionArg#TARGET} parameter.
 */
public class CardCountCondition extends CountCondition {

	public CardCountCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected int getCountForPlayer(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			EntityFilter filter,
			EntityFilter cardFilter) {
		CardList hand = player.getHand();
		EntityFilter activeFilter = filter != null ? filter : cardFilter;
		if (activeFilter == null) {
			return hand.getCount();
		} else {
			return (int) hand.stream().filter(activeFilter.matcher(context, player, source)).count();
		}
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
