package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.HasCard;

/**
 * Fired when a tap action is activated on an entity. This includes Forge effects on hand cards and activated abilities
 * on minions.
 * <p>
 * The target of this event is the card associated with the tapped entity.
 */
public final class TapEvent extends CardEvent implements HasCard {

	public TapEvent(GameContext context, int playerId, Card card) {
		super(GameEventType.TAP_ACTIVATED, true, context, playerId, -1, card);
	}
}
