package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns the number of minions, including permanents, on the board, unfiltered.
 */
public class BoardCountValueProvider extends EntityCountValueProvider {

	public BoardCountValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		return player.getMinions().size();
	}
}
