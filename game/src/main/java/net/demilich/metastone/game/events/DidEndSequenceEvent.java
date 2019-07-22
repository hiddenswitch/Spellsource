package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class DidEndSequenceEvent extends GameEvent {
	public DidEndSequenceEvent(GameContext context) {
		super(context, context.getActivePlayerId(), -1);
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.DID_END_SEQUENCE;
	}
}
