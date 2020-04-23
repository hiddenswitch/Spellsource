package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * The maximum amount of Lun the player has increased. Occurs at the start of the turn too.
 */
public class MaxManaChangedEvent extends ValueEvent {

	public MaxManaChangedEvent(GameContext context, int playerId, int change) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.MAX_MANA, context, playerId, -1, null, change);
	}
}
