package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

import java.util.stream.Stream;

public class MinionCountCondition extends Condition {

	private static final long serialVersionUID = 121161930124020375L;

	public MinionCountCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		TargetPlayer targetPlayer = desc.containsKey(ConditionArg.TARGET_PLAYER) ? (TargetPlayer) desc.get(ConditionArg.TARGET_PLAYER)
				: TargetPlayer.SELF;

		Stream<Minion> minions;

		switch (targetPlayer) {
			case BOTH:
				minions = Stream.concat(player.getMinions().stream(), context.getOpponent(player).getMinions().stream());
				break;
			case OPPONENT:
				minions = context.getOpponent(player).getMinions().stream();
				break;
			case SELF:
			case OWNER:
				minions = player.getMinions().stream();
				break;
			default:
				minions = Stream.empty();
				break;
		}
		EntityFilter filter = (EntityFilter) desc.getOrDefault(ConditionArg.CARD_FILTER, desc.get(ConditionArg.FILTER));
		if (filter != null) {
			minions = minions.filter(filter.matcher(context, player, source));
		}
		int targetValue = desc.getInt(ConditionArg.VALUE);
		ComparisonOperation operation = (ComparisonOperation) desc.get(ConditionArg.OPERATION);
		return SpellUtils.evaluateOperation(operation, (int) minions.count(), targetValue);
	}

}
