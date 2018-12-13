package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;

/**
 * Converts the {@link net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider} in {@link
 * DynamicDescriptionArg#VALUE} to a string.
 */
public class ValueDescription extends DynamicDescription {
	public ValueDescription(DynamicDescriptionDesc desc) {
		super(desc);
	}

	@Override
	public String resolveFinalString(GameContext context, Player player, Card card) {
		return "" + getDesc().getValue(DynamicDescriptionArg.VALUE, context, player, card, card, 0);
	}
}
