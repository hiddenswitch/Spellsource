package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Concatenates the strings and {@link DynamicDescription} objects specified in its {@link
 * DynamicDescriptionArg#DESCRIPTIONS} array.
 */
public class MetaDescription extends DynamicDescription {
	public MetaDescription(DynamicDescriptionDesc desc) {
		super(desc);
	}

	@Override
	public String resolveFinalString(GameContext context, Player player, Entity entity) {
		DynamicDescription[] dynamicDescriptions = (DynamicDescription[]) getDesc().get(DynamicDescriptionArg.DESCRIPTIONS);
		String description = "";
		for (DynamicDescription dynamicDescription : dynamicDescriptions) {
			description += dynamicDescription.resolveFinalString(context, player, entity);
		}
		return description;
	}
}
