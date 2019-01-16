package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires whenever a card is added to the deck via a {@link net.demilich.metastone.game.logic.GameLogic#shuffleToDeck(Player,
 * Card)} or {@link net.demilich.metastone.game.logic.GameLogic#insertIntoDeck(Player, Card, int)} effect.
 */
public class CardAddedToDeckTrigger extends AbstractCardTrigger {

	public CardAddedToDeckTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.CARD_ADDED_TO_DECK;
	}
}
