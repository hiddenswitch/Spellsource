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
	public GameEventType getEventType() {
		return GameEventType.MANA_MODIFIED;
	}

	@Override
	public int getValue() {
		return value;
	}
}
