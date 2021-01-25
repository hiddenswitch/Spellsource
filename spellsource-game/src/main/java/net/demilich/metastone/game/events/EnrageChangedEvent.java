package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

import static com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.ENRAGE_CHANGED;

/**
 * An {@link net.demilich.metastone.game.entities.Actor} with an {@link net.demilich.metastone.game.cards.Attribute#ENRAGABLE}
 * attribute was wounded or is now fully healed.
 */
public class EnrageChangedEvent extends BasicGameEvent {

	public EnrageChangedEvent(GameContext context, Entity target) {
		super(ENRAGE_CHANGED, context, target, target.getOwner(), -1);
	}
}
