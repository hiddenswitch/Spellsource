package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * A minion was killed. The source is <b>not</b> the source of the killing effect, it is just the minion.
 * <p>
 * The minion is in the {@link net.demilich.metastone.game.targeting.Zones#GRAVEYARD} when this is fired.
 */
public final class KillEvent extends BasicGameEvent {

	public KillEvent(GameContext context, Entity victim) {
		super(EventTypeEnum.KILL, context, context.getPlayer(victim.getOwner()), victim, victim);
	}
}
