package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * {@link net.demilich.metastone.game.cards.Attribute#MAX_HP} was increased.
 *
 * @see net.demilich.metastone.game.spells.trigger.MaxHpIncreasedTrigger for the corresponding trigger.
 */
public final class MaxHpIncreasedEvent extends ValueEvent {

	public MaxHpIncreasedEvent(GameContext context, Entity target, int amount, int sourcePlayerId) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.MAX_HP_INCREASED, context, target.getOwner(), sourcePlayerId, target, amount);
	}
}
