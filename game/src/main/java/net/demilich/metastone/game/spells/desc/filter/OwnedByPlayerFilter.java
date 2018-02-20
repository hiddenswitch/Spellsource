package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;

public class OwnedByPlayerFilter extends EntityFilter {

	public OwnedByPlayerFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		TargetPlayer targetPlayer = (TargetPlayer) desc.get(FilterArg.TARGET_PLAYER);

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
			default:
				break;

		}

		return false;
	}
}
