package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.PreDamageEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import com.hiddenswitch.spellsource.rpc.Spellsource.DamageTypeMessage.DamageType;

/**
 * Triggers as damage caused only if the damage is not fatigue damage.
 */
public class PreDeflectDamageTrigger extends PreDamageTrigger {

	public PreDeflectDamageTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		if (!super.innerQueues(event, enchantment, host)) {
			return false;
		}

		PreDamageEvent preDamageEvent = (PreDamageEvent) event;
		if (preDamageEvent.getDamageType().contains(DamageType.DEFLECT)) {
			return true;
		}

		return false;
	}
}
