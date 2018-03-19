package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;

public class RaceFilter extends EntityFilter {

	public RaceFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		Race race = (Race) desc.get(FilterArg.RACE);
		return entity.getAttribute(Attribute.RACE) == race;
	}

}
