package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;

/**
 * Lun has been gained or lost.
 */
public final class ModifyCurrentManaEvent extends ValueEvent {

	public ModifyCurrentManaEvent(GameContext context, int targetPlayerId, int amount) {
		super(GameEventType.MANA_MODIFIED, true, context, targetPlayerId, targetPlayerId, null, amount);
	}
}
