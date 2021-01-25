package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;

/**
 * Matches entities whose {@link Entity#getRace()} is specifically {@link Race#ALL}.
 */
public final class AmalgamRaceFilter extends EntityFilter {

	public AmalgamRaceFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		return entity != null && entity.getRace() == Race.ALL;
	}
}
