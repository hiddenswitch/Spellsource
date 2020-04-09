package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class SpellCastedTrigger extends AbstractCardTrigger {

	public SpellCastedTrigger(EventTriggerDesc desc) {
		super(desc);
		EventTriggerDesc clone = desc.clone();
		clone.put(EventTriggerArg.CARD_TYPE, CardType.SPELL);
		setDesc(clone);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		return super.innerQueues(event, host);
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.SPELL_CASTED;
	}

}
