package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public final class DrainEvent extends GameEvent implements HasValue {

	private int value;
	private Entity source;

	public DrainEvent(GameContext context, Entity source, int sourcePlayerId, int value) {
		super(context, sourcePlayerId, sourcePlayerId);
		this.value = value;
		this.source = source;
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public Entity getEventSource() {
		return source;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.DRAIN;
	}

	@Override
	public int getValue() {
		return value;
	}
}
