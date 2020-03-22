package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class PreGameStartEvent extends GameEvent {


	public PreGameStartEvent(GameContext context, int playerId) {
		super(context, playerId, -1);
	}
	
	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.PRE_GAME_START;
	}
}
