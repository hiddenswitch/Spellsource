package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Converts the {@link net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider} in {@link
 * DynamicDescriptionArg#VALUE} to a string.
 */
public class ValueDescription extends DynamicDescription {
	public ValueDescription(DynamicDescriptionDesc desc) {
		super(desc);
	}

	@Override
	public String resolveFinalString(GameContext context, Player player, Entity entity) {
		return Integer.toString(getValue(context, player, entity));
	}

	protected int getValue(GameContext context, Player player, Entity entity) {
		return getDesc().getValue(DynamicDescriptionArg.VALUE, context, player, entity, entity, 0);
	}
}
