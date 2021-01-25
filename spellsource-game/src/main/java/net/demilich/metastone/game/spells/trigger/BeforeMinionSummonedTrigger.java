package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class BeforeMinionSummonedTrigger extends AbstractSummonTrigger {

	public BeforeMinionSummonedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTriggerDesc create() {
		return new EventTriggerDesc(BeforeMinionSummonedTrigger.class);
	}

	@Override
	public GameEventType interestedIn() {
		return com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.BEFORE_SUMMON;
	}

}
