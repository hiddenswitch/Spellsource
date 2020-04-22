package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

/**
 * A card was discovered (sourced).
 */
public class DiscoverEvent extends CardEvent {

	public DiscoverEvent(GameContext context, int playerId, Card card) {
		super(EventTypeEnum.DISCOVER, context, playerId, -1,card);
	}
}
