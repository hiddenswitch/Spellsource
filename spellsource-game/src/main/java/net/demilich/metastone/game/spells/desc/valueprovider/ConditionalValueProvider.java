package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * If {@link ValueProviderArg#CONDITION} is true, returns {@link ValueProviderArg#IF_TRUE}, otherwise returns {@link
 * ValueProviderArg#IF_FALSE}.
 */
public class ConditionalValueProvider extends ValueProvider {

	public ConditionalValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(
			GameContext context,
			Player player,
			Entity target,
			Entity source,
			int ifTrue,
			int ifFalse,
			Condition condition) {
		return condition.isFulfilled(context, player, source, target) ? ifTrue : ifFalse;
	}

}
