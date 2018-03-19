package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

import java.util.Map;

public class AndFilter extends EntityFilter {

	public static AndFilter create() {
		return new AndFilter(new EntityFilterDesc(AndFilter.class));
	}

	public static AndFilter create(EntityFilter... filters) {
		Map<FilterArg, Object> arguments = new EntityFilterDesc(AndFilter.class);
		arguments.put(FilterArg.FILTERS, filters);
		return new AndFilter(new EntityFilterDesc(arguments));
	}

	public AndFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		EntityFilter[] filters = (EntityFilter[]) desc.get(FilterArg.FILTERS);
		if (filters == null) {
			return true;
		}
		for (EntityFilter filter : filters) {
			if (!filter.matches(context, player, entity, host)) {
				return false;
			}
		}
		return true;
	}

}
