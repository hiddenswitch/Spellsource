package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

public class DamagedFilter extends EntityFilter {

	private static final long serialVersionUID = 7550596241765068737L;

	public DamagedFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		if (entity instanceof Actor) {
			return ((Actor) entity).isWounded();
		}
		return false;
	}

}

