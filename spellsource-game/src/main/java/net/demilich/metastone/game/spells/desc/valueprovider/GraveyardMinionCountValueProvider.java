package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

/**
 * Provide the count of minions in the graveyard optionally matching {@link ValueProviderArg#FILTER}.
 * <p>
 *Does not read {@link ValueProviderArg#TARGET}
 */
public class GraveyardMinionCountValueProvider extends EntityCountValueProvider {

	public GraveyardMinionCountValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(GameContext context, Player player, Entity target, Entity host, EntityFilter filter) {
		return (int) player.getGraveyard().stream()
				.filter(e -> e instanceof Minion)
				.filter(e -> matches(context, player, e, host, filter))
				.count();
	}
}
