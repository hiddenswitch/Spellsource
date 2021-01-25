package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Concatenates the strings and {@link DynamicDescription} objects specified in its {@link
 * DynamicDescriptionArg#DESCRIPTIONS} array.
 * <p>
 * An element in the array can be a {@link DynamicDescriptionDesc} dynamic description argument <b>or</b> a {@code
 * String} literal. Make sure to include the appropriate whitespace, concatenation is not performed with spaces.
 *
 * @see PluralDescription for a common use of combining elements to get a good plural dynamic description.
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
