package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class WillEndSequenceEvent extends GameEvent {
	private static final long serialVersionUID = -3036987677623376548L;

	public WillEndSequenceEvent(GameContext context) {
		super(context, context.getActivePlayerId(), context.getActivePlayerId());
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.WILL_END_SEQUENCE;
	}

	@Override
	public boolean isClientInterested() {
		return false;
	}
}
