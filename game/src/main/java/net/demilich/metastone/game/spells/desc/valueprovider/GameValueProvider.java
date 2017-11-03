package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.spells.GameValue;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class GameValueProvider extends ValueProvider {
	public GameValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		GameValue gameValue = (GameValue) desc.get(ValueProviderArg.GAME_VALUE);
		switch (gameValue) {
			case LAST_MANA_COST:
				return (int) context.getEnvironment().get(Environment.LAST_MANA_COST);
			case SPELL_VALUE:
				// Query the top of the stack since that's almost always what's intended.
				return context.getSpellValueStack().peekFirst();
			default:
				break;
		}
		return 0;
	}

}
