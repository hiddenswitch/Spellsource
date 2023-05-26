package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.Attribute;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.SummonEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public abstract class AbstractSummonTrigger extends EventTrigger {

	public AbstractSummonTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		SummonEvent summonEvent = (SummonEvent) event;

		String race = (String) getDesc().get(EventTriggerArg.RACE);
		if (race != null && !Race.hasRace(event.getGameContext(), summonEvent.getTarget(), race)) {
			return false;
		}

		Attribute requiredAttribute = (Attribute) getDesc().get(EventTriggerArg.REQUIRED_ATTRIBUTE);
		// Special case DEATHRATTLES
		if (requiredAttribute == Attribute.DEATHRATTLES
				&& !summonEvent.getTarget().getSourceCard().hasAttribute(requiredAttribute)) {
			return false;
		} else if (requiredAttribute != null && !summonEvent.getTarget().hasAttribute(requiredAttribute)) {
			return false;
		}

		if (onlyPlayedFromHandOrDeck() &&
				(summonEvent.getSource().getEntityType() != EntityType.CARD
						|| summonEvent.getSource().getSourceCard().getCardType() != CardType.MINION
						|| !summonEvent.getSource().hasAttribute(Attribute.PLAYED_FROM_HAND_OR_DECK))) {
			return false;
		}

		// Don't trigger for permanents EVER
		if (summonEvent.getTarget().hasAttribute(Attribute.PERMANENT)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean fires(GameEvent event, Entity host, int playerId) {
		SummonEvent summonEvent = (SummonEvent) event;
		// Don't trigger if the minion is no longer on the board
		if (!summonEvent.getTarget().isInPlay() || summonEvent.getTarget().isRemovedPeacefully()) {
			return false;
		}
		return super.fires(event, host, playerId);
	}

	protected boolean onlyPlayedFromHandOrDeck() {
		return false;
	}

	public abstract com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType interestedIn();
}
