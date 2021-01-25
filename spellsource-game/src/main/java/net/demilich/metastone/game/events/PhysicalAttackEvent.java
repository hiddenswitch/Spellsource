package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;

/**
 * An attacker (the {@link net.demilich.metastone.game.targeting.EntityReference#EVENT_SOURCE}) attacked the defender
 * and dealt {@link net.demilich.metastone.game.spells.desc.valueprovider.EventValueProvider} damage.
 */
public class PhysicalAttackEvent extends ValueEvent {
	public PhysicalAttackEvent(GameContext context, Actor attacker, Actor defender, int damageDealt) {
		super(GameEventType.PHYSICAL_ATTACK, true, context, context.getPlayer(attacker.getOwner()), attacker, defender, damageDealt);
	}

	PhysicalAttackEvent(GameEventType eventType, boolean isClientInterested, GameContext context, Actor attacker, Actor defender, int damageDealt) {
		super(eventType, isClientInterested, context, context.getPlayer(attacker.getOwner()), attacker, defender, damageDealt);
	}

	public Actor getAttacker() {
		return (Actor) getSource();
	}

	public int getDamageDealt() {
		return getValue();
	}

	public Actor getDefender() {
		return (Actor) getTarget();
	}
}
