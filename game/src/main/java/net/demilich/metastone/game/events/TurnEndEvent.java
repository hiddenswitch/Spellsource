package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class TurnEndEvent extends GameEvent {

	public TurnEndEvent(GameContext context, int playerId) {
		super(context, playerId, -1);
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.TURN_END;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
