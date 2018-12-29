package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.AfterSummonEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class AfterMinionPlayedTrigger extends AfterMinionSummonedTrigger {

	public AfterMinionPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean onlyPlayedFromHandOrDeck() {
		return true;
	}

}
