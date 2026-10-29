package net.demilich.metastone.game.spells.desc.valueprovider;

import java.util.List;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

public class AttributeCounter extends ValueProvider {

	public AttributeCounter(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(
			GameContext context,
			Player player,
			Entity target,
			Entity host,
			EntityReference targetReference,
			Attribute attribute) {
		List<Entity> entities = context.resolveTarget(player, host, targetReference);
		int count = 0;
		for (Entity entity : entities) {
			if (entity.hasAttribute(attribute)) {
				count++;
			}
		}
		return count;
	}

}
