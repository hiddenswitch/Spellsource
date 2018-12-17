package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

public class SilenceEvent extends GameEvent {

	private static final long serialVersionUID = 3433426548153484102L;
	private final Actor target;

	public SilenceEvent(GameContext context, int playerId, Actor target) {
		super(context, playerId, -1);
		this.target = target;
	}

	@Override
	public Entity getEventTarget() {
		return getTarget();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.SILENCE;
	}

	public Actor getTarget() {
		return target;
	}

}
