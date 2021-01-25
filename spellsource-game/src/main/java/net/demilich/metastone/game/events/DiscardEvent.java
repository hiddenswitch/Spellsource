package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

/**
 * A card was discarded.
 * <p>
 * At the moment this event is fired, the card is still in the {@link com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones#HAND}.
 * The discard can be cancelled by removing the {@link net.demilich.metastone.game.cards.Attribute#DISCARDED} attribute
 * from the {@link net.demilich.metastone.game.targeting.EntityReference#EVENT_TARGET}.
 */
public class DiscardEvent extends CardEvent {

	public DiscardEvent(GameContext context, int playerId, Card card) {
		super(GameEventType.DISCARD, true, context, playerId, -1, card);
	}

	DiscardEvent(GameEventType GameEventType, GameContext context, int playerId, Card card) {
		super(GameEventType, true, context, playerId, -1, card);
	}

	@Override
	public boolean isPowerHistory() {
		return true;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s discarded %s", context.getPlayer(playerId).getName(), getSourceCard().getName());
	}
}
