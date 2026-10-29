package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Default;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

/**
 * Evaluates to {@code true} if the player's count of something in {@link #getCountForPlayer(GameContext, Player,
 * Entity, Entity, EntityFilter, EntityFilter)} satisfies the {@link ConditionArg#OPERATION} with {@link ConditionArg#VALUE}.
 */
public abstract class CountCondition extends Condition {

	public CountCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilledForTarget(GameContext context, Player player, Entity source, Entity target, TargetPlayer targetPlayer) {
		// Delegate TargetPlayer.BOTH handling to isFulfilled
		if (targetPlayer == TargetPlayer.BOTH) {
			return super.isFulfilledForTarget(context, player, source, target, TargetPlayer.SELF);
		}
		return super.isFulfilledForTarget(context, player, source, target, targetPlayer);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			int value,
			@Default(string = "EQUAL") ComparisonOperation operation,
			TargetPlayer targetPlayer,
			EntityFilter filter,
			EntityFilter cardFilter) {
		int cardCount;
		if (targetPlayer == TargetPlayer.BOTH) {
			cardCount = getCountForPlayer(context, player, source, target, filter, cardFilter)
					+ getCountForPlayer(context, context.getOpponent(player), source, target, filter, cardFilter);
		} else {
			cardCount = getCountForPlayer(context, player, source, target, filter, cardFilter);
		}
		return SpellUtils.evaluateOperation(operation, cardCount, value);
	}

	protected abstract int getCountForPlayer(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			EntityFilter filter,
			EntityFilter cardFilter);

	@Override
	protected boolean usesFilter() {
		return false;
	}
}
