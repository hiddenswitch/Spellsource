package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class BoardChangedEvent extends GameEvent {

	private static final long serialVersionUID = 3708195806240240462L;

	public BoardChangedEvent(GameContext context) {
		super(context, -1, -1);
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.BOARD_CHANGED;
	}

}
