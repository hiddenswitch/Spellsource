package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * The {@link net.demilich.metastone.game.targeting.EntityReference#EVENT_TARGET} was healed for {@link
 * net.demilich.metastone.game.spells.desc.valueprovider.EventValueProvider} healing.
 * <p>
 * Excess healing can be retrieved using {@link net.demilich.metastone.game.spells.trigger.ExcessHealingTrigger}. This
 * is the amount requested to heal, not the amount actually healed (which may be zero for a full-health minion, for
 * example).
 */
public final class HealEvent extends ValueEvent {

	public HealEvent(GameContext context, int playerId, Entity target, int healing) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.HEAL, true, context, target.getOwner(), playerId, target, healing);
	}
}