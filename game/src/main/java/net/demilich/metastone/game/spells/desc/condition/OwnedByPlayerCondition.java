package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwnedByPlayerCondition extends Condition {

	private static Logger LOGGER = LoggerFactory.getLogger(OwnedByPlayerCondition.class);

	public OwnedByPlayerCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		TargetPlayer targetPlayer = (TargetPlayer) desc.get(ConditionArg.TARGET_PLAYER);
		LOGGER.debug("isFulfilled {} {}: targetPlayer={}, player.getId()={}, context.getActivePlayerId()={}, target.getOwner={}",
				context.getGameId(),
				source,
				targetPlayer,
				player.getId(),
				context.getActivePlayerId(),
				target == null ? -1 : target.getOwner());
		switch (targetPlayer) {
			case ACTIVE:
				return context.getActivePlayerId() == player.getId();
			case INACTIVE:
				return context.getActivePlayerId() != player.getId();
			case BOTH:
				return true;
			case OPPONENT:
				return target.getOwner() != player.getId();
			case SELF:
				return target.getOwner() == player.getId();
			case PLAYER_1:
				return target.getOwner() == GameContext.PLAYER_1;
			case PLAYER_2:
				return target.getOwner() == GameContext.PLAYER_2;
			default:
				break;

		}
		return false;
	}

}
