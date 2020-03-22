package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

public class DiscoverEvent extends GameEvent {

	private final Actor target;

	public DiscoverEvent(GameContext context, int playerId, Actor target) {
		super(context, playerId, -1);
		this.target = target;
	}

	public DiscoverEvent(GameContext context, int playerId) {
		super(context, playerId, -1);
		this.target = null;
	}

	@Override
	public Entity getEventTarget() {
		return getTarget();
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.DISCOVER;
	}

	public Actor getTarget() {
		return target;
	}

}
