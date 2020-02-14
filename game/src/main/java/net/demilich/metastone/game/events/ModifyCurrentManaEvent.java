package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class ModifyCurrentManaEvent extends GameEvent implements HasValue {

	private final int value;

	public ModifyCurrentManaEvent(GameContext context, int targetPlayerId, int amount) {
		super(context, targetPlayerId, targetPlayerId);
		this.value = amount;
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.MANA_MODIFIED;
	}

	@Override
	public int getValue() {
		return value;
	}
}
