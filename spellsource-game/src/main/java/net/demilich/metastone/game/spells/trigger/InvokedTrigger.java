package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class InvokedTrigger extends AbstractCardTrigger {

	public InvokedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.INVOKED;
	}
}
