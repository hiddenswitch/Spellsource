package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.Attribute;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.events.SummonEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public abstract class AbstractSummonTrigger extends EventTrigger {

	public AbstractSummonTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		SummonEvent summonEvent = (SummonEvent) event;

		String race = (String) getDesc().get(EventTriggerArg.RACE);
		if (race != null && !Race.hasRace(event.getGameContext(), summonEvent.getMinion(), race)) {
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

	@Override
	public boolean fires(GameEvent event) {
		SummonEvent summonEvent = (SummonEvent) event;
		// Don't trigger if the minion is no longer on the board
		if (!summonEvent.getMinion().isInPlay() || summonEvent.getMinion().isRemovedPeacefully()) {
			return false;
		}
		return super.fires(event);
	}

	protected boolean onlyPlayedFromHandOrDeck() {
		return false;
	}

	public abstract com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn();
}
