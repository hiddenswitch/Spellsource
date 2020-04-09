package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.cards.Attribute;

/**
 * The base class for triggers that fire off card-adjacent effects.
 * <p>
 * This supports the {@link EventTriggerArg#REQUIRED_ATTRIBUTE}, {@link EventTriggerArg#RACE} and {@link
 * EventTriggerArg#CARD_TYPE} arguments on the {@link HasCard#getSourceCard()} entity.
 */
public abstract class AbstractCardTrigger extends EventTrigger {
	public AbstractCardTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		HasCard cardPlayedEvent = (HasCard) event;
		CardType cardType = (CardType) getDesc().get(EventTriggerArg.CARD_TYPE);
		if (cardType != null && !GameLogic.isCardType(cardPlayedEvent.getSourceCard().getCardType(), cardType)) {
			return false;
		}

		Attribute requiredAttribute = (Attribute) getDesc().get(EventTriggerArg.REQUIRED_ATTRIBUTE);
		if (requiredAttribute != null && !cardPlayedEvent.getSourceCard().getAttributes().containsKey(requiredAttribute)) {
			return false;
		}

		String race = (String) getDesc().get(EventTriggerArg.RACE);
		if (race != null && !Race.hasRace(event.getGameContext(), cardPlayedEvent.getSourceCard(), race)) {
			return false;
		}

		return true;
	}
}
