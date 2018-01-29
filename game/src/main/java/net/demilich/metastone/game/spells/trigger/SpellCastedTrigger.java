package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class SpellCastedTrigger extends AbstractCardTrigger {

	public SpellCastedTrigger(EventTriggerDesc desc) {
		super(desc);
		this.desc.put(EventTriggerArg.CARD_TYPE, CardType.SPELL);
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
