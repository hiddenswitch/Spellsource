package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;

/**
 * Evaluates to {@code true} if the {@link ConditionArg#TARGET_PLAYER} has card-count [ {@link ConditionArg#OPERATION} ]
 * {@link ConditionArg#VALUE} cards.
 */
public abstract class CountCondition extends Condition {

	public CountCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected final boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return false;
	}

	@Override
	public final boolean isFulfilled(GameContext context, Player player, Entity source, Entity target) {
		boolean invert = getDesc().getBool(ConditionArg.INVERT);
		ConditionDesc desc = getDesc();
		TargetPlayer targetPlayer = desc.containsKey(ConditionArg.TARGET_PLAYER) ? (TargetPlayer) desc.get(ConditionArg.TARGET_PLAYER)
				: TargetPlayer.SELF;
		int cardCount = 0;
		int targetValue = desc.getValue(ConditionArg.VALUE, context, player, target, source, 0);
		ComparisonOperation operation = (ComparisonOperation) desc.get(ConditionArg.OPERATION);
		switch (targetPlayer) {
			case EITHER:
				return (SpellUtils.evaluateOperation(operation, getCountForPlayer(context, player, source, target), targetValue) || SpellUtils.evaluateOperation(operation, getCountForPlayer(context, context.getOpponent(player), source, target), targetValue)) != invert;
			case BOTH:
				cardCount = getCountForPlayer(context, player, source, target) + getCountForPlayer(context, context.getOpponent(player), source, target);
				break;
			case OPPONENT:
				cardCount = getCountForPlayer(context, context.getOpponent(player), source, target);
				break;
			case SELF:
				cardCount = getCountForPlayer(context, player, source, target);
				break;
			case ACTIVE:
				cardCount = getCountForPlayer(context, context.getActivePlayer(), source, target);
				break;
			case INACTIVE:
				cardCount = getCountForPlayer(context, context.getOpponent(context.getActivePlayer()), source, target);
				break;
			case OWNER:
				cardCount = getCountForPlayer(context, context.getPlayer(source.getOwner()), source, target);
				break;
			case PLAYER_1:
				cardCount = getCountForPlayer(context, context.getPlayer1(), source, target);
				break;
			case PLAYER_2:
				cardCount = getCountForPlayer(context, context.getPlayer2(), source, target);
			default:
				break;

		}

		return SpellUtils.evaluateOperation(operation, cardCount, targetValue) != invert;
	}

	protected abstract int getCountForPlayer(GameContext context, Player player, Entity source, Entity target);
}

