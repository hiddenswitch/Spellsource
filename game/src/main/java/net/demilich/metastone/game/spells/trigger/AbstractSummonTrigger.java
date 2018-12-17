package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.SummonEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public abstract class AbstractSummonTrigger extends EventTrigger {

	private static final long serialVersionUID = 7182271853097113973L;

	public AbstractSummonTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		SummonEvent summonEvent = (SummonEvent) event;
		Race race = (Race) getDesc().get(EventTriggerArg.RACE);
		if (race != null && !summonEvent.getMinion().getSourceCard().hasRace(race)) {
			return false;
		}

		Attribute requiredAttribute = (Attribute) getDesc().get(EventTriggerArg.REQUIRED_ATTRIBUTE);
		// Special case DEATHRATTLES
		if (requiredAttribute == Attribute.DEATHRATTLES
				&& !summonEvent.getMinion().getSourceCard().hasAttribute(requiredAttribute)) {
			return false;
		} else if (requiredAttribute != null && !summonEvent.getMinion().hasAttribute(requiredAttribute)) {
			return false;
		}

		// Don't trigger for permanents EVER
		if (summonEvent.getMinion().hasAttribute(Attribute.PERMANENT)) {
			return false;
		}

		return true;
	}

	public abstract GameEventType interestedIn();
}
