package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class OrFilter extends EntityFilter {

	public OrFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		EntityFilter[] filters = (EntityFilter[]) getDesc().get(EntityFilterArg.FILTERS);
		if (filters == null) {
			return false;
		}
		for (EntityFilter filter : filters) {
			if (filter.matches(context, player, entity, host)) {
				return true;
			}
		}
		return false;
	}

}
