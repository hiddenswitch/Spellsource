package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OwnedByPlayerCondition extends Condition {

	private static Logger LOGGER = LoggerFactory.getLogger(OwnedByPlayerCondition.class);

	public OwnedByPlayerCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return false;
	}

	@Override
	public boolean isFulfilled(GameContext context, Player player, Entity source, Entity target) {
		TargetPlayer targetPlayer = (TargetPlayer) getDesc().get(ConditionArg.TARGET_PLAYER);
		switch (targetPlayer) {
			case ACTIVE:
				return context.getActivePlayerId() == player.getId();
			case INACTIVE:
				return context.getActivePlayerId() != player.getId();
			case BOTH:
				return false;
			case EITHER:
				return true;
			case OPPONENT:
				return target != null && target.getOwner() != player.getId();
			case SELF:
				return target != null && target.getOwner() == player.getId();
			case PLAYER_1:
				return target != null && target.getOwner() == GameContext.PLAYER_1;
			case PLAYER_2:
				return target != null && target.getOwner() == GameContext.PLAYER_2;
			default:
				break;

		}
		return false;
	}

}
