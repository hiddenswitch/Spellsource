package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.GameValue;
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
			return (int) context.getEnvironment().get(Environment.SPELL_VALUE);
		default:
			break;
		}
		return 0;
	}

}
