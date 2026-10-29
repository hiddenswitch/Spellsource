package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class HeroPowerDamageValueProvider extends ValueProvider {

	public HeroPowerDamageValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(GameContext context, Player player, Entity target, Entity host, int value) {
		return context.getLogic().applyHeroPowerDamage(player, value);
	}

}
