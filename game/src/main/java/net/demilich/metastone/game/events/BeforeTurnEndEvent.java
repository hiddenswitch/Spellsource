package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public final class BeforeTurnEndEvent extends GameEvent {

	public BeforeTurnEndEvent(GameContext context, int playerId) {
		super(context, playerId, -1);
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.BEFORE_TURN_END;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
