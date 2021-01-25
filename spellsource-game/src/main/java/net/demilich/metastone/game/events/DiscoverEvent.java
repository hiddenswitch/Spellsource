package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

/**
 * A card was discovered (sourced).
 */
public class DiscoverEvent extends CardEvent {

	public DiscoverEvent(GameContext context, int playerId, Card card) {
		super(GameEventType.DISCOVER, context, playerId, -1,card);
	}
}
