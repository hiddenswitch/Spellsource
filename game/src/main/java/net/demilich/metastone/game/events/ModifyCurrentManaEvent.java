package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * Lun has been gained or lost.
 */
public final class ModifyCurrentManaEvent extends ValueEvent {

	public ModifyCurrentManaEvent(GameContext context, int targetPlayerId, int amount) {
		super(EventTypeEnum.MANA_MODIFIED, true, context, targetPlayerId, targetPlayerId, null, amount);
	}
}
