package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;

import java.util.List;

public class HasAttackedFilter extends EntityFilter {

	public HasAttackedFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		Actor actor = (Actor) entity;
		return actor.getMaxNumberOfAttacks() > actor.getAttributeValue(Attribute.NUMBER_OF_ATTACKS);
	}
}
