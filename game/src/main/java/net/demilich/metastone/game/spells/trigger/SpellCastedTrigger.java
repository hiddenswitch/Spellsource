package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
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
	protected boolean fire(GameEvent event, Entity host) {
		return super.fire(event, host);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.SPELL_CASTED;
	}

}
