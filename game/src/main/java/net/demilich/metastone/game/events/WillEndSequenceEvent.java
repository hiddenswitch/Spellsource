package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class WillEndSequenceEvent extends GameEvent {
	public WillEndSequenceEvent(GameContext context) {
		super(context, context.getActivePlayerId(), -1);
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.WILL_END_SEQUENCE;
	}
}
