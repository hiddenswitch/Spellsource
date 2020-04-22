package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.client.models.DamageTypeEnum;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.AbstractDamageEvent;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class PreDamageTrigger extends EventTrigger {

	public PreDamageTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		// For compatibility reasons, splash damage will not trigger this
		var damageEvent = (AbstractDamageEvent) event;
		if (enchantment.getSourceCard().getDesc().getFileFormatVersion() <= 1
				&& damageEvent.getDamageType().contains(DamageTypeEnum.SPLASH)) {
			return false;
		}
		return true;
	}

	@Override
	public EventTypeEnum interestedIn() {
		return EventTypeEnum.PRE_DAMAGE;
	}

}

