package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class MaxManaChangedEvent extends GameEvent implements HasValue {
	private final int change;

	public MaxManaChangedEvent(GameContext context, int playerId, int change) {
		super(context, playerId, -1);
		this.change = change;
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.MAX_MANA;
	}

	@Override
	public boolean isClientInterested() {
		return false;
	}

	@Override
	public int getValue() {
		return change;
	}

	public int getChangeInMaxMana() {
		return change;
	}
}
