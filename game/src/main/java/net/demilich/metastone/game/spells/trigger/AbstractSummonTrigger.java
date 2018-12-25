package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.SummonEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public abstract class AbstractSummonTrigger extends EventTrigger {

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

		if (onlyPlayedFromHandOrDeck() &&
				(summonEvent.getSource().getEntityType() != EntityType.CARD
						|| summonEvent.getSource().getSourceCard().getCardType() != CardType.MINION
						|| !summonEvent.getSource().hasAttribute(Attribute.PLAYED_FROM_HAND_OR_DECK))) {
			return false;
		}

		// Don't trigger for permanents EVER
		if (summonEvent.getMinion().hasAttribute(Attribute.PERMANENT)) {
			return false;
		}

		return true;
	}

	protected boolean onlyPlayedFromHandOrDeck() {
		return false;
	}

	public abstract GameEventType interestedIn();
}
