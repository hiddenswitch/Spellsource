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
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.DID_END_SEQUENCE;
	}
}
