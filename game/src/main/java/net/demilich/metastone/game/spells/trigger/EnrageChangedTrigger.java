package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class EnrageChangedTrigger extends EventTrigger {

	private static final long serialVersionUID = -1495422576895225769L;

	public EnrageChangedTrigger() {
		this(new EventTriggerDesc(EnrageChangedTrigger.class));
	}

	public EnrageChangedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		return event.getEventTarget() == host;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.ENRAGE_CHANGED;
	}

}
