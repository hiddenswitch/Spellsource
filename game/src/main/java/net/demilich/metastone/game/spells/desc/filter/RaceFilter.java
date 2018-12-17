package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;

public class RaceFilter extends EntityFilter {

	private static final long serialVersionUID = 390874869729734871L;

	public RaceFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		Race race = (Race) getDesc().get(EntityFilterArg.RACE);
		return entity.getSourceCard().hasRace(race);
	}

}
