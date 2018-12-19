package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.spells.TargetPlayer;

public abstract class ZoneContainsFilter extends EntityFilter {
	public ZoneContainsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		EntityFilterDesc desc = getDesc();
		if (desc.containsKey(EntityFilterArg.TARGET_PLAYER)) {
			switch ((TargetPlayer) desc.get(EntityFilterArg.TARGET_PLAYER)) {
				case OPPONENT:
					player = context.getOpponent(player);
					break;
				default:
					break;
			}
		}
		for (Entity e : getZone(player)) {
			Card card = e.getSourceCard();
			if (entity.getSourceCard().getCardId().equalsIgnoreCase(card.getCardId())) {
				return true;
			}
		}
		return false;
	}

	protected abstract EntityZone<? extends Entity> getZone(Player player);
}
