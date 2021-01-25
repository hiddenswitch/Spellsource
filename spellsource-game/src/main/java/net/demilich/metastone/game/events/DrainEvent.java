package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * A target was {@link net.demilich.metastone.game.spells.DrainSpell} drained.
 */
public final class DrainEvent extends ValueEvent {

	public DrainEvent(GameContext context, Entity source, Entity target, int sourcePlayerId, int value) {
		super(com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.DRAIN, context, sourcePlayerId, source, target, value);
	}
}
