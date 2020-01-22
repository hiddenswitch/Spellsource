package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

public class BeforePhysicalAttackEvent extends PhysicalAttackEvent {
	public BeforePhysicalAttackEvent(GameContext context, Actor attacker, Actor defender) {
		super(context, attacker, defender, 0);
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.BEFORE_PHYSICAL_ATTACK;
	}

	@Override
	public boolean isClientInterested() {
		return false;
	}
}
