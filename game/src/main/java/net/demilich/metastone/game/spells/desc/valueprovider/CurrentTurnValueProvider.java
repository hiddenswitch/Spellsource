package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class CurrentTurnValueProvider extends ValueProvider {

	private static final long serialVersionUID = -8994922899053721841L;

	public CurrentTurnValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		return context.getTurn();
	}
}

