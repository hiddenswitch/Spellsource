package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;

public final class AfterPhysicalAttackEvent extends PhysicalAttackEvent {

	public AfterPhysicalAttackEvent(GameContext context, Actor attacker, Actor defender, int damageDealt) {
		super(context, attacker, defender, damageDealt);
	}

	@Override
	public GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.AFTER_PHYSICAL_ATTACK;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
