package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;

/**
 * Returns the number of {@link net.demilich.metastone.game.entities.Actor} entities that were destroyed during the
 * evaluation of this sequence.
 */
public final class DestroyedThisSequenceValueProvider extends ValueProvider {

	public DestroyedThisSequenceValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		return (int) context.getEnvironment().getOrDefault(Environment.DESTROYED_THIS_SEQUENCE_COUNT, 0);
	}
}
