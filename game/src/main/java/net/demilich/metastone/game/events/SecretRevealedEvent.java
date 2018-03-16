package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class SecretRevealedEvent extends GameEvent implements HasCard {
	private final Card secretCard;

	public SecretRevealedEvent(GameContext context, Card secret, int playerId) {
		super(context, playerId, secret.getOwner());
		this.secretCard = secret;
	}
	
	@Override
	public Entity getEventTarget() {
		return getCard();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.SECRET_REVEALED;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	@Override
	public Card getCard() {
		return secretCard;
	}
}
