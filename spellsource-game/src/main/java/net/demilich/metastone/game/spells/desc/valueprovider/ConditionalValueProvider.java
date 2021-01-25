package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * When the {@link ValueProviderArg#CONDITION} evalutes to {@code true}, return the value from {@link
 * ValueProviderArg#IF_TRUE}. Otherwise, return the value in {@link ValueProviderArg#IF_FALSE}.
 */
public class ConditionalValueProvider extends ValueProvider {

	public ConditionalValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity source) {
		int ifTrue = getDesc().getValue(ValueProviderArg.IF_TRUE, context, player, target, source, 0);
		int ifFalse = getDesc().getValue(ValueProviderArg.IF_FALSE, context, player, target, source, 0);

		Condition condition = (Condition) getDesc().get(ValueProviderArg.CONDITION);
		return condition.isFulfilled(context, player, source, target) ? ifTrue : ifFalse;
	}

}
