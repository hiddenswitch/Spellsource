package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.targeting.EntityReference;
import org.jetbrains.annotations.NotNull;

/**
 * The card's effects have occurred and it has been moved to the graveyard.
 */
public class AfterCardPlayedEvent extends CardEvent implements HasCard {

	public AfterCardPlayedEvent(GameContext context, int playerId, EntityReference cardReference) {
		super(EventTypeEnum.AFTER_PLAY_CARD, context, playerId, context.tryFind(cardReference), null, (Card) context.tryFind(cardReference));
	}
}
