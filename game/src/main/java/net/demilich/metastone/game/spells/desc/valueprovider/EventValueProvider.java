package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.HasValue;

/**
 * Retrieves the {@link HasValue#getValue()} of the event being processed.
 */
public class EventValueProvider extends ValueProvider {

	public EventValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		return context.getEventValue();
	}
}
