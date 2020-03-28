package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.QuestPlayedEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class PactPlayedTrigger extends QuestPlayedTrigger {

	public PactPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		var questPlayedEvent = (QuestPlayedEvent) event;
		return questPlayedEvent.getQuest().isPact();
	}
}
