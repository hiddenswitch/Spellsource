package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.ShuffledEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class ShuffledOnlyOriginalCopiesTrigger extends ShuffledTrigger {

	public ShuffledOnlyOriginalCopiesTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		ShuffledEvent shuffledEvent = (ShuffledEvent) event;
		if (shuffledEvent.isExtraCopy()) {
			return false;
		}
		return super.innerQueues(event, enchantment, host);
	}
}
