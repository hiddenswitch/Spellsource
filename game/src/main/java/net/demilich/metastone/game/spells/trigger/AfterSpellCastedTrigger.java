package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.AfterSpellCastedEvent;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class AfterSpellCastedTrigger extends AbstractCardTrigger {

	public AfterSpellCastedTrigger(EventTriggerDesc desc) {
		super(desc);
		EventTriggerDesc clone = desc.clone();
		clone.put(EventTriggerArg.CARD_TYPE, CardType.SPELL);
		setDesc(clone);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		AfterSpellCastedEvent spellCastedEvent = (AfterSpellCastedEvent) event;

		EntityType targetEntityType = (EntityType) getDesc().get(EventTriggerArg.TARGET_ENTITY_TYPE);
		if (targetEntityType != null
				&& (spellCastedEvent.getEventTarget() == null || targetEntityType != spellCastedEvent.getEventTarget().getEntityType())) {
			return false;
		}

		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.AFTER_SPELL_CASTED;
	}

}
