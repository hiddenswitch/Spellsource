package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public final class DrainEvent extends GameEvent implements HasValue {

	private int value;
	private Entity source;
	private Entity target;

	public DrainEvent(GameContext context, Entity source, Entity target, int sourcePlayerId, int value) {
		super(context, target.getOwner(), sourcePlayerId);
		this.value = value;
		this.source = source;
		this.target = target;
	}

	@Override
	public Entity getEventTarget() {
		return target;
	}

	@Override
	public Entity getEventSource() {
		return source;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.DRAIN;
	}

	@Override
	public int getValue() {
		return value;
	}
}
