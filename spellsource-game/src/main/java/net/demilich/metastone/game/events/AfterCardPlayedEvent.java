package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * The card's effects have occurred and it has been moved to the graveyard.
 */
public class AfterCardPlayedEvent extends CardEvent implements HasCard {

	public AfterCardPlayedEvent(GameContext context, int playerId, EntityReference cardReference) {
		super(GameEventType.AFTER_PLAY_CARD, context, playerId, context.tryFind(cardReference), null, (Card) context.tryFind(cardReference));
	}
}
