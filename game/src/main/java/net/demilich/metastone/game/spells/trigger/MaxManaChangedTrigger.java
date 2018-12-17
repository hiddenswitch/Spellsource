package net.demilich.metastone.game.spells.trigger;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class MaxManaChangedTrigger extends EventTrigger {
	private static final long serialVersionUID = 5026463515842923157L;

	public MaxManaChangedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	protected boolean fire(GameEvent event, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.MAX_MANA;
	}
}


