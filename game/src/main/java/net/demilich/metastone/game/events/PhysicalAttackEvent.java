package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

/**
 * An attacker (the {@link net.demilich.metastone.game.targeting.EntityReference#EVENT_SOURCE}) attacked the defender
 * and dealt {@link net.demilich.metastone.game.spells.desc.valueprovider.EventValueProvider} damage.
 */
public class PhysicalAttackEvent extends ValueEvent {
	public PhysicalAttackEvent(GameContext context, Actor attacker, Actor defender, int damageDealt) {
		super(EventTypeEnum.PHYSICAL_ATTACK, true, context, context.getPlayer(attacker.getOwner()), attacker, defender, damageDealt);
	}

	PhysicalAttackEvent(EventTypeEnum eventType, boolean isClientInterested, GameContext context, Actor attacker, Actor defender, int damageDealt) {
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
