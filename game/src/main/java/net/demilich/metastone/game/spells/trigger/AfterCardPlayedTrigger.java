package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class AfterCardPlayedTrigger extends AbstractCardTrigger {

	public AfterCardPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTrigger create() {
		return new EventTriggerDesc(AfterCardPlayedTrigger.class).create();
	}

	@Override
	public GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.AFTER_PLAY_CARD;
	}
}
