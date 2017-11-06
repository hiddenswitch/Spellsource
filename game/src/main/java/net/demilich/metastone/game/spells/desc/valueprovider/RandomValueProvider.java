package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class RandomValueProvider extends ValueProvider {

	public RandomValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		int min = desc.getValue(ValueProviderArg.MIN, context, player, target, host, 0);
		int max = desc.getValue(ValueProviderArg.MAX, context, player, target, host, 0);
		int diff = max - min;
		return min + context.getLogic().random(diff + 1);
	}

}
