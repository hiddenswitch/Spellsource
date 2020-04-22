package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.events.SilenceEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires whenever an {@link Actor} is {@link Attribute#SILENCED}.
 * <p>
 * The {@link SilenceEvent} populates the silenced actor into {@link net.demilich.metastone.game.targeting.EntityReference#EVENT_TARGET}.
 */
public class SilenceTrigger extends EventTrigger {

	public SilenceTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.SILENCE;
	}

}
