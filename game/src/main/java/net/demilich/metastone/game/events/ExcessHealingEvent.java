package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * A {@code target} was healed with excess healing (i.e. more healing applied than there were hitpoints to heal).
 */
public final class ExcessHealingEvent extends ValueEvent {

	public ExcessHealingEvent(GameContext context, Player player, Entity source, Entity target, int excess) {
		super(EventTypeEnum.EXCESS_HEAL, context, player, source, target, excess);
	}
}
