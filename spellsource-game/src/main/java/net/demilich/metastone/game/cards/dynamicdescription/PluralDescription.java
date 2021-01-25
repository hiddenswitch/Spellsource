package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Appends the {@link DynamicDescriptionArg#VALUE} with {@link DynamicDescriptionArg#DESCRIPTION1} if the value is
 * singular, otherwise appends {@link DynamicDescriptionArg#DESCRIPTION2}.
 * <p>
 * For <b>example</b>, this code will read the {@code RESERVED_INTEGER_1} attribute on the card and render it with the
 * appropriate pluralized count.
 * <pre>
 *   "description": "Summon [0 copies] of a friendly minion.",
 *   "dynamicDescription": [
 *     // Observe this is an array, each element corresponds to each square-bracketed number in your description
 *     {"class": "PluralDescription"
 *        // Observe the space, since it is concatenating to the right of your number.
 *        // description1 corresponds to singular, description2 to plural
 *       "description1": " copy",
 *       "description2": " copies",
 *       "value": {
 *           "class": "AttributeValueProvider",
 *           "attribute": "RESERVED_INTEGER_1",
 *           "target": "SELF"
 *       }
 *   ]
 * </pre>
 */
public class PluralDescription extends ValueDescription {
	public PluralDescription(DynamicDescriptionDesc desc) {
		super(desc);
	}

	@Override
	public String resolveFinalString(GameContext context, Player player, Entity entity) {
		if (!getDesc().containsKey(DynamicDescriptionArg.DESCRIPTION1)
				&& !getDesc().containsKey(DynamicDescriptionArg.DESCRIPTION2)) {
			return super.resolveFinalString(context, player, entity);
		}
		int value = getValue(context, player, entity);
		String valueString = super.resolveFinalString(context, player, entity);

		valueString += value == 1
				? getDesc().getDynamicDescription(DynamicDescriptionArg.DESCRIPTION1, context, player, entity)
				: getDesc().getDynamicDescription(DynamicDescriptionArg.DESCRIPTION2, context, player, entity);

		return valueString;
	}
}
