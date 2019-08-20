package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

public class GraveyardCountCondition extends CountCondition {

	public GraveyardCountCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected int getCountForPlayer(GameContext context, Player player, Entity source, Entity target) {
		int count = 0;
		EntityFilter filter = (EntityFilter) getDesc().get(ConditionArg.FILTER);
		for (Entity deadEntity : player.getGraveyard()) {
			if (deadEntity instanceof Minion && !deadEntity.isRemovedPeacefully() && (filter == null || filter.matches(context, player, source, deadEntity))) {
				count++;
			}
		}
		return count;
	}

}
