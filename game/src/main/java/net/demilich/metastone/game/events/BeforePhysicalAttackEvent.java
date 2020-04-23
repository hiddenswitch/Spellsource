package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;

/**
 * Before the attack happens and damage dealt is calculated.
 * <p>
 * This event fires after targets are resolved and possibly overriden, but before stealth is lost, the number of attacks
 * is modified or immunity is applied.
 */
public class BeforePhysicalAttackEvent extends PhysicalAttackEvent {
	public BeforePhysicalAttackEvent(GameContext context, Actor attacker, Actor defender) {
		super(EventTypeEnum.BEFORE_PHYSICAL_ATTACK, false, context, attacker, defender, 0);
	}
}
