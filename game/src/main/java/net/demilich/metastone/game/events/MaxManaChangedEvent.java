package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;

/**
 * The maximum amount of Lun the player has increased. Occurs at the start of the turn too.
 */
public class MaxManaChangedEvent extends ValueEvent {

	public MaxManaChangedEvent(GameContext context, int playerId, int change) {
		super(EventTypeEnum.MAX_MANA, true, context, playerId, playerId, null, change);
	}
}
