package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class CardRevealedTrigger extends AbstractCardTrigger {
	public CardRevealedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEvent.EventTypeEnum interestedIn() {
		return GameEvent.EventTypeEnum.REVEAL_CARD;
	}
}
