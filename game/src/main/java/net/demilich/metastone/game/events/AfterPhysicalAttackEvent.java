package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

public final class AfterPhysicalAttackEvent extends PhysicalAttackEvent {

	public AfterPhysicalAttackEvent(GameContext context, Actor attacker, Actor defender, int damageDealt) {
		super(context, attacker, defender, damageDealt);
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.AFTER_PHYSICAL_ATTACK;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
