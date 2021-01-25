package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

import static com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.REVEAL_CARD;

/**
 * A card was revealed. Signals to the client to simply render it.
 * <p>
 * Fired with {@link net.demilich.metastone.game.spells.RevealCardSpell} and some special spells that need to show cards
 * one after another, like with {@link }
 */
public final class CardRevealedEvent extends CardEvent {

	public CardRevealedEvent(GameContext context, int playerId, Card card) {
		super(REVEAL_CARD, true, context, playerId, -1, card);
	}
}

