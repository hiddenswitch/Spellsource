package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DamageEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.DamageType;

/**
 * Triggers as damage caused only if the damage is not fatigue damage.
 */
public class NonFatigueDamageCausedTrigger extends DamageCausedTrigger {

	public NonFatigueDamageCausedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		if (!super.fire(event, host)) {
			return false;
		}

		DamageEvent damageEvent = (DamageEvent) event;
		if (damageEvent.getDamageType() == DamageType.FATIGUE) {
			return false;
		}

		return true;
	}
}
