package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

import java.util.stream.Stream;

/**
 * Returns the number of minions (non permanent, not removed peacefully) that are in the casting {@code player}'s {@link
 * com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones#GRAVEYARD}.
 */
public class GraveyardMinionCountValueProvider extends EntityCountValueProvider {

	public GraveyardMinionCountValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		EntityFilter filter = (EntityFilter) getDesc().get(ValueProviderArg.FILTER);
		Stream<Entity> stream = player.getGraveyard()
				.stream()
				.filter(Entity::diedOnBattlefield);
		if (filter != null) {
			stream = stream.filter(filter.matcher(context, player, host));
		}
		return (int) stream.count();
	}
}
