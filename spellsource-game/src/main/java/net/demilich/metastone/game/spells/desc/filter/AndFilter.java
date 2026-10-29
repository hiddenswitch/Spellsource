package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Evaluates to {@code true} when an {@code entity} passes all the sub-filters in {@link EntityFilterArg#FILTERS}, or if
 * the filters list is empty.
 *
 * @see #create() for the pass-through {@code true} filter, which will match all entities.
 * @see #create(EntityFilter...) to create an {@code AndFilter} from an array of existing filters.
 * @see NullFilter for a filter that returns {@code false}.
 */
public class AndFilter extends EntityFilter {
	private static final AndFilter INSTANCE = new AndFilter(new EntityFilterDesc(AndFilter.class));

	public static AndFilter create() {
		return INSTANCE;
	}

	public static AndFilter create(EntityFilter... filters) {
		if (filters == null || filters.length == 0) {
			return INSTANCE;
		}
		EntityFilterDesc arguments = new EntityFilterDesc(AndFilter.class);
		arguments.put(EntityFilterArg.FILTERS, filters);
		return new AndFilter(arguments);
	}

	public AndFilter(EntityFilterDesc desc) {
		super(desc);
	}

	protected boolean test(GameContext context, Player player, Entity entity, Entity host, EntityFilter[] filters) {
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

	@Override
	public boolean equals(Object other) {
		return super.equals(other);
	}
}
