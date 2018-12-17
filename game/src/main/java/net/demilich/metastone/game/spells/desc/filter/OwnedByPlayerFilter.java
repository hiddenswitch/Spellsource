package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;

public class OwnedByPlayerFilter extends EntityFilter {

	private static final long serialVersionUID = 2656311284649075826L;

	public OwnedByPlayerFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		TargetPlayer targetPlayer = (TargetPlayer) getDesc().get(EntityFilterArg.TARGET_PLAYER);

		switch (targetPlayer) {
			case ACTIVE:
				return context.getActivePlayerId() == player.getId();
			case INACTIVE:
				return context.getActivePlayerId() != player.getId();
			case BOTH:
				return true;
			case OPPONENT:
				return entity.getOwner() != player.getId();
			case SELF:
				return entity.getOwner() == player.getId();
			case OWNER:
				return entity.getOwner() == host.getOwner();
			case PLAYER_1:
				return entity.getOwner() == GameContext.PLAYER_1;
			case PLAYER_2:
				return entity.getOwner() == GameContext.PLAYER_2;
			default:
				break;

		}

		return false;
	}
}
