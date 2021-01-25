package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

/**
 * Computes the highest value of the attribute in the specified list of {@code target} entities and returns all with
 * that highest value.
 */
public class HighestAttributeFilter extends EntityFilter {

	public HighestAttributeFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		Attribute attribute = (Attribute) getDesc().get(EntityFilterArg.ATTRIBUTE);
		EntityReference targetReference = (EntityReference) getDesc().get(EntityFilterArg.TARGET);
		List<Entity> entities = context.resolveTarget(player, entity, targetReference);
		int highest = getHighestInList(context, entities, attribute);
		return AttributeValueProvider.provideValueForAttribute(context, attribute, entity) >= highest;
	}

	private static int getHighestInList(GameContext context, List<Entity> entities, Attribute attribute) {
		int highest = Integer.MIN_VALUE;
		for (Entity entity : entities) {
			int attributeValue = AttributeValueProvider.provideValueForAttribute(context, attribute, entity);
			if (attributeValue > highest) {
				highest = attributeValue;
			}
		}
		return highest;
	}
}
