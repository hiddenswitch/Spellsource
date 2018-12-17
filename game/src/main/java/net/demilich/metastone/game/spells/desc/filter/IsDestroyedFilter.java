package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class IsDestroyedFilter extends EntityFilter {

	private static final long serialVersionUID = 2152444199265427112L;

	public IsDestroyedFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		return entity.isDestroyed();
	}

}
