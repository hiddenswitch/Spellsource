package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;

public class BeforePhysicalAttackEvent extends PhysicalAttackEvent {
	public BeforePhysicalAttackEvent(GameContext context, Actor attacker, Actor defender) {
		super(context, attacker, defender, 0);
	}

	@Override
	public GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.BEFORE_PHYSICAL_ATTACK;
	}

	@Override
	public boolean isClientInterested() {
		return false;
	}
}
