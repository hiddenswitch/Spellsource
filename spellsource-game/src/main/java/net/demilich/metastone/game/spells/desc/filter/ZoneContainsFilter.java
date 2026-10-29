package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityZone;

import java.util.Objects;

public abstract class ZoneContainsFilter extends EntityFilter {

	public ZoneContainsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		EntityZone<? extends Entity> zone = getZone(player);
		// By object reference
		if (zone.contains(entity)) {
			return true;
		}
		for (Entity zoneEntity : zone) {
			if (Objects.equals(zoneEntity.getSourceCard().getCardId(), entity.getSourceCard().getCardId())) {
				return true;
			}
		}
		return false;
	}

	protected abstract EntityZone<? extends Entity> getZone(Player player);
}
