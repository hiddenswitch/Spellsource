package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.condition.Condition;

public class ConditionalValueProvider extends ValueProvider {

	public ConditionalValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity source) {
		int ifTrue = getDesc().getInt(ValueProviderArg.IF_TRUE);
		int ifFalse = getDesc().getInt(ValueProviderArg.IF_FALSE);

		Condition condition = (Condition) getDesc().get(ValueProviderArg.CONDITION);
		return condition.isFulfilled(context, player, source, target) ? ifTrue : ifFalse;
	}

}
