package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DamageEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import com.hiddenswitch.spellsource.client.models.DamageTypeEnum;

/**
 * Triggers as damage caused only if the damage is not fatigue damage.
 */
public class NonFatigueDamageCausedTrigger extends DamageCausedTrigger {

	public NonFatigueDamageCausedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		if (!super.innerQueues(event, host)) {
			return false;
		}

		DamageEvent damageEvent = (DamageEvent) event;
		if (damageEvent.getDamageType() == DamageTypeEnum.FATIGUE) {
			return false;
		}

		return true;
	}
}
