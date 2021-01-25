package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * A conditional description will use {@link DynamicDescriptionArg#DESCRIPTION1} when the condition is {@code true}, or
 * {@link DynamicDescriptionArg#DESCRIPTION2} when it is {@code false}.
 */
public class ConditionalDescription extends DynamicDescription {
	public ConditionalDescription(DynamicDescriptionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(DynamicDescriptionDesc desc, GameContext context, Player player, Entity entity) {
		Condition condition = (Condition) desc.get(DynamicDescriptionArg.CONDITION);
		if (condition == null) {
			return false;
		}
		return condition.isFulfilled(context, player, entity, entity);
	}

	@Override
	public String resolveFinalString(GameContext context, Player player, Entity entity) {
		return isFulfilled(getDesc(), context, player, entity)
				? getDesc().getDynamicDescription(DynamicDescriptionArg.DESCRIPTION1, context, player, entity)
				: getDesc().getDynamicDescription(DynamicDescriptionArg.DESCRIPTION2, context, player, entity);
	}
}
