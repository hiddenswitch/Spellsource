package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.HasCard;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.utils.Attribute;

public abstract class AbstractCardTrigger extends EventTrigger {
	public AbstractCardTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		HasCard cardPlayedEvent = (HasCard) event;
		CardType cardType = (CardType) getDesc().get(EventTriggerArg.CARD_TYPE);
		if (cardType != null && !cardPlayedEvent.getCard().getCardType().isCardType(cardType)) {
			return false;
		}

		Attribute requiredAttribute = (Attribute) getDesc().get(EventTriggerArg.REQUIRED_ATTRIBUTE);
		if (requiredAttribute != null && !cardPlayedEvent.getCard().getAttributes().containsKey(requiredAttribute)) {
			return false;
		}

		Race race = (Race) getDesc().get(EventTriggerArg.RACE);
		if (race != null && !cardPlayedEvent.getCard().hasRace(race)) {
			return false;
		}

		return true;
	}
}
