package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class InvokedTrigger extends AbstractCardTrigger {

	private static final long serialVersionUID = 2279683049865378789L;

	public InvokedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.INVOKED;
	}
}
