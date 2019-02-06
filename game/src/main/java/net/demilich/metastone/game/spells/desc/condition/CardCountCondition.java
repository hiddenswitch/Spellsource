package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;

public class CardCountCondition extends Condition {

	public CardCountCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		TargetPlayer targetPlayer = desc.containsKey(ConditionArg.TARGET_PLAYER) ? (TargetPlayer) desc.get(ConditionArg.TARGET_PLAYER)
				: TargetPlayer.SELF;
		int cardCount = 0;
		switch (targetPlayer) {
			case EITHER:
				ConditionDesc playerDesc = desc.clone();
				playerDesc.put(ConditionArg.TARGET_PLAYER, TargetPlayer.SELF);
				ConditionDesc opponentDesc = desc.clone();
				opponentDesc.put(ConditionArg.TARGET_PLAYER, TargetPlayer.OPPONENT);
				return isFulfilled(context, player, playerDesc, source, target) || isFulfilled(context, player, opponentDesc, source, target);
			case BOTH:
				cardCount = player.getHand().getCount() + context.getOpponent(player).getHand().getCount();
				break;
			case OPPONENT:
				cardCount = context.getOpponent(player).getHand().getCount();
				break;
			case SELF:
				cardCount = player.getHand().getCount();
				break;
			case ACTIVE:
				cardCount = context.getActivePlayer().getHand().getCount();
				break;
			case INACTIVE:
				cardCount = context.getOpponent(context.getActivePlayer()).getHand().getCount();
				break;
			case OWNER:
				cardCount = context.getPlayer(source.getOwner()).getHand().getCount();
				break;
			case PLAYER_1:
				cardCount = context.getPlayer1().getHand().getCount();
				break;
			case PLAYER_2:
				cardCount = context.getPlayer2().getHand().getCount();
			default:
				break;

		}
		int targetValue = desc.getInt(ConditionArg.VALUE);
		ComparisonOperation operation = (ComparisonOperation) desc.get(ConditionArg.OPERATION);
		return SpellUtils.evaluateOperation(operation, cardCount, targetValue);
	}

}
