package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public final class ExcessHealingEvent extends GameEvent implements HasValue {
	private final Entity target;
	private final int excess;

	public ExcessHealingEvent(GameContext context, int playerId, Entity target, int excess) {
		super(context, target.getOwner(), playerId);
		this.target = target;
		this.excess = excess;
	}

	@Override
	public Entity getEventTarget() {
		return getTarget();
	}

	@Override
	public EventTypeEnum getEventType() {
		return EventTypeEnum.EXCESS_HEAL;
	}

	@Override
	public int getValue() {
		return excess;
	}

	public Entity getTarget() {
		return target;
	}

	@Override
	public boolean isClientInterested() {
		return false;
	}
}
