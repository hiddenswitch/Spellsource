package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class GameStartEvent extends GameEvent {


	private static final long serialVersionUID = -3461643118844211762L;

	public GameStartEvent(GameContext context, int playerId) {
		super(context, playerId, -1);
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.GAME_START;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
