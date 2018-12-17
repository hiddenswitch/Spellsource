package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class PreGameStartEvent extends GameEvent {


	private static final long serialVersionUID = -4850267897740260061L;

	public PreGameStartEvent(GameContext context, int playerId) {
		super(context, playerId, -1);
	}
	
	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.PRE_GAME_START;
	}
}
