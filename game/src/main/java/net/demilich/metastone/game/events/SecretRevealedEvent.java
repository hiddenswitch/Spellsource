package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

/**
 * A secret was revealed.
 */
public class SecretRevealedEvent extends CardEvent {

	public SecretRevealedEvent(GameContext context, Card secret, int playerId) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.SECRET_REVEALED, context, playerId, secret.getOwner(), secret);
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s fired!", getSourceCard().getName());
	}
}
