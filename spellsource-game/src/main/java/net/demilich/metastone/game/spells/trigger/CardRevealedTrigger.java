package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class CardRevealedTrigger extends AbstractCardTrigger {
	public CardRevealedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.REVEAL_CARD;
	}
}
