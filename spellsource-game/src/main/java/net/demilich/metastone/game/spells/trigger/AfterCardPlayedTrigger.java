package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class AfterCardPlayedTrigger extends AbstractCardTrigger {

	public AfterCardPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTrigger create() {
		return new EventTriggerDesc(AfterCardPlayedTrigger.class).create();
	}

	@Override
	public GameEventType interestedIn() {
		return com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.AFTER_PLAY_CARD;
	}
}
