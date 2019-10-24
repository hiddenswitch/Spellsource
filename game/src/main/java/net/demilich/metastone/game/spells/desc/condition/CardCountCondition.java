package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

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

