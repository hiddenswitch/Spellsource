package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Appends the {@link DynamicDescriptionArg#VALUE} with {@link DynamicDescriptionArg#DESCRIPTION1} if the value is singular, otherwise appends {@link DynamicDescriptionArg#DESCRIPTION2}.
 */
public class PluralDescription extends ValueDescription {
	public PluralDescription(DynamicDescriptionDesc desc) {
		super(desc);
	}

	@Override
	public String resolveFinalString(GameContext context, Player player, Entity entity) {
		int value = getValue(context, player, entity);
		String valueString = super.resolveFinalString(context, player, entity);

		valueString += value == 1
				? getDesc().getDynamicDescription(DynamicDescriptionArg.DESCRIPTION1, context, player, entity)
				: getDesc().getDynamicDescription(DynamicDescriptionArg.DESCRIPTION2, context, player, entity);

		return valueString;
	}
}
