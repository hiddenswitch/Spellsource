package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

public final class RaceEqualsFilter extends EntityFilter {

	private static final long serialVersionUID = 5613648268970486909L;

	public RaceEqualsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		Entity comparedTo = context.resolveSingleTarget(player, host, (EntityReference) getDesc().get(EntityFilterArg.SECONDARY_TARGET));

		return entity.getRace().hasRace(comparedTo.getRace());
	}
}
