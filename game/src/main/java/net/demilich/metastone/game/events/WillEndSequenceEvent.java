package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class WillEndSequenceEvent extends GameEvent {
	public WillEndSequenceEvent(GameContext context) {
		super(context, context.getActivePlayerId(), context.getActivePlayerId());
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.WILL_END_SEQUENCE;
	}

	@Override
	public boolean isClientInterested() {
		return false;
	}
}
