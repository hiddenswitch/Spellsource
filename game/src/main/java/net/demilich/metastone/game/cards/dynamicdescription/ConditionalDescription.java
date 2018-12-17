package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * A conditional description will use {@link DynamicDescriptionArg#DESCRIPTION1} when the condition is {@code true}, or
 * {@link DynamicDescriptionArg#DESCRIPTION2} when it is {@code false}.
 */
public class ConditionalDescription extends DynamicDescription {
	private static final long serialVersionUID = -1956050724453549718L;

	public ConditionalDescription(DynamicDescriptionDesc desc) {
		super(desc);
	}

	@Override
	public String resolveFinalString(GameContext context, Player player, Card card) {
		Condition condition = (Condition) getDesc().get(DynamicDescriptionArg.CONDITION);
		if (condition == null) {
			return "";
		}
		DynamicDescriptionDesc description1 = (DynamicDescriptionDesc) getDesc().get(DynamicDescriptionArg.DESCRIPTION1);
		DynamicDescriptionDesc description2 = (DynamicDescriptionDesc) getDesc().get(DynamicDescriptionArg.DESCRIPTION2);

		if (condition.isFulfilled(context, player, card, card)) {
			return description1.create().resolveFinalString(context, player, card);
		} else return description2.create().resolveFinalString(context, player, card);
	}
}
