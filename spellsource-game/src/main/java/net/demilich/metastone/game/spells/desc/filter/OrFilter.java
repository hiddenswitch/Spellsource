package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class OrFilter extends EntityFilter {

	public static OrFilter create(EntityFilter... filters) {
		EntityFilterDesc arguments = new EntityFilterDesc(OrFilter.class);
		arguments.put(EntityFilterArg.FILTERS, filters);
		return new OrFilter(arguments);
	}

	public OrFilter(EntityFilterDesc desc) {
		super(desc);
	}

	protected boolean test(GameContext context, Player player, Entity entity, Entity host, EntityFilter[] filters) {
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

	@Override
	public boolean equals(Object other) {
		return super.equals(other);
	}
}
