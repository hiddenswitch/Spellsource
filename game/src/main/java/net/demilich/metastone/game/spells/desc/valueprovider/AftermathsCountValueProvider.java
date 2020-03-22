package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns the number of aftermaths triggered by the {@link ValueProviderArg#TARGET_PLAYER}.
 */
public class AftermathsCountValueProvider extends ValueProvider {

	public AftermathsCountValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		return (int) context.getAftermaths().getAftermaths()
				.stream()
				.filter(item -> item.getPlayerId() == player.getId())
				.count();
	}
}
