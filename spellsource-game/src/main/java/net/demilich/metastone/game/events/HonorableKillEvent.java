package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * Fires when a source deals exactly lethal damage to a target (damage == target's remaining HP before the hit).
 * The source is the damage dealer, the target is the killed entity.
 */
public final class HonorableKillEvent extends BasicGameEvent {

	public HonorableKillEvent(GameContext context, Entity source, Entity target) {
		super(GameEventType.HONORABLE_KILL, context, context.getPlayer(source.getOwner()), source, target);
	}
}
