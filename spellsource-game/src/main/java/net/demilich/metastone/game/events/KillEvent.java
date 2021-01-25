package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * A minion was killed. The source is <b>not</b> the source of the killing effect, it is just the minion.
 * <p>
 * The minion is in the {@link com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones#GRAVEYARD} when this is fired.
 */
public final class KillEvent extends BasicGameEvent {

	public KillEvent(GameContext context, Entity victim) {
		super(GameEventType.KILL, true, context, context.getPlayer(victim.getOwner()), victim, victim);
	}
}
