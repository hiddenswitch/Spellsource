package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;

public class GraveyardCountCondition extends Condition {

	private static final long serialVersionUID = -951783119492724140L;

	public GraveyardCountCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		int count = 0;
		for (Entity deadEntity : player.getGraveyard()) {
			if (deadEntity instanceof Minion && !deadEntity.isRemovedPeacefully()) {
				count++;
			}
		}
		int targetValue = desc.getInt(ConditionArg.VALUE);
		ComparisonOperation operation = (ComparisonOperation) desc.get(ConditionArg.OPERATION);
		return SpellUtils.evaluateOperation(operation, count, targetValue);
	}

}
