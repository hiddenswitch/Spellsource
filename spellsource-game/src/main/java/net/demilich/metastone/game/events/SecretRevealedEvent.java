package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

/**
 * A secret was revealed.
 */
public class SecretRevealedEvent extends CardEvent {

	public SecretRevealedEvent(GameContext context, Card secret, int playerId) {
		super(com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.SECRET_REVEALED, context, playerId, secret.getOwner(), secret);
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s fired!", getSourceCard().getName());
	}
}
