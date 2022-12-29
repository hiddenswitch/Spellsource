package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Only returns entities that have a base odd-cost.
 */
public class OddCostFilter extends ManaCostFilter {

	public OddCostFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		return getManaCost(context, player, entity) % 2 == 1;
	}

	@Override
	protected int getManaCost(GameContext context, Player player, Entity entity) {
		return entity.getSourceCard().getBaseManaCost();
	}
}

