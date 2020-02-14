package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * Fired whenever {@link net.demilich.metastone.game.cards.Attribute#MAX_HP} is increased.
 *
 * @see net.demilich.metastone.game.spells.trigger.MaxHpIncreasedTrigger for the corresponding trigger.
 */
public final class MaxHpIncreasedEvent extends GameEvent implements HasValue {

	private final Entity target;
	private final int amount;

	public MaxHpIncreasedEvent(GameContext context, Entity target, int amount, int sourcePlayerId) {
		super(context, target.getOwner(), sourcePlayerId);
		this.target = target;
		this.amount = amount;
	}

	@Override
	public Entity getEventTarget() {
		return target;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.MAX_HP_INCREASED;
	}

	@Override
	public int getValue() {
		return amount;
	}
}
