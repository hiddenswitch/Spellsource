package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

import java.util.Map;

/**
 * Evaluates to {@code true} for every {@code target} if all of the filters in {@link EntityFilterArg#FILTERS} also
 * evaluate to true for the target.
 * <p>
 * If {@link EntityFilterArg#FILTERS} is empty, evaluates to {@code true}.
 */
public class AndFilter extends EntityFilter {

	public static AndFilter create() {
		return new AndFilter(new EntityFilterDesc(AndFilter.class));
	}

	public static AndFilter create(EntityFilter... filters) {
		Map<EntityFilterArg, Object> arguments = new EntityFilterDesc(AndFilter.class);
		arguments.put(EntityFilterArg.FILTERS, filters);
		return new AndFilter(new EntityFilterDesc(arguments));
	}

	public AndFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		EntityFilter[] filters = (EntityFilter[]) getDesc().get(EntityFilterArg.FILTERS);
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
