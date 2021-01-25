package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

/**
 * Lun was overloaded (i.e. spent ahead of time).
 *
 * @see net.demilich.metastone.game.cards.Attribute#OVERLOAD
 */
public final class OverloadEvent extends ValueEvent {

	public OverloadEvent(GameContext context, int playerId, Card card, int manaCrystalsOverloaded) {
		super(com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.OVERLOAD, context, playerId, playerId, card, manaCrystalsOverloaded);
	}
}
