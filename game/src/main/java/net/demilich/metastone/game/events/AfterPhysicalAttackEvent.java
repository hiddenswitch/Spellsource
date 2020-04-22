package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;

import static com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.AFTER_PHYSICAL_ATTACK;

/**
 * The damage has been dealt, all effects due to damage have been resolved, and now the physical attack has complete.
 * <p>
 * Destroyed actors are still on the board, but {@link net.demilich.metastone.game.logic.GameLogic#markAsDestroyed(Actor)}.
 */
public final class AfterPhysicalAttackEvent extends PhysicalAttackEvent {

	public AfterPhysicalAttackEvent(GameContext context, Actor attacker, Actor defender, int damageDealt) {
		super(AFTER_PHYSICAL_ATTACK, true, context, attacker, defender, damageDealt);
	}
}
