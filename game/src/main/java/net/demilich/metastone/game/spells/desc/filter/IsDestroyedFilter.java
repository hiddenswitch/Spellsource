package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

public class IsDestroyedFilter extends EntityFilter {

	public IsDestroyedFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		return entity.isDestroyed();
	}

}
