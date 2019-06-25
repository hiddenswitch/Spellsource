package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

public class HighestAttributeFilter extends EntityFilter {

	public HighestAttributeFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		Attribute attribute = (Attribute) getDesc().get(EntityFilterArg.ATTRIBUTE);
		EntityReference targetReference = (EntityReference) getDesc().get(EntityFilterArg.TARGET);
		List<Entity> entities = context.resolveTarget(player, entity, targetReference);
		int highest = getHighestInList(entities, attribute);
		return getAttributeValue(entity, attribute) >= highest;
	}

	private static int getAttributeValue(Entity entity, Attribute attribute) {
		if (attribute == Attribute.ATTACK) {
			return ((Actor) entity).getAttack();
		} else if (attribute == Attribute.HP) {
			return ((Actor) entity).getHp();
		}
		return entity.getAttributeValue(attribute);
	}

	private static int getHighestInList(List<Entity> entities, Attribute attribute) {
		int highest = Integer.MIN_VALUE;
		for (Entity entity : entities) {
			int attributeValue = getAttributeValue(entity, attribute);
			if (attributeValue > highest) {
				highest = attributeValue;
			}
		}
		return highest;
	}
}
