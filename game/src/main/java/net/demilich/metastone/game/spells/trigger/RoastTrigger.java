package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class RoastTrigger extends AbstractCardTrigger {

	private static final long serialVersionUID = 2719726835694540063L;

	public RoastTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.ROASTED;
	}
}
