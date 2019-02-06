package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class SecretPlayedEvent extends GameEvent implements HasCard {

	private final Card secretCard;

	public SecretPlayedEvent(GameContext context, int playerId, Card secretCard) {
		super(context, playerId, -1);
		this.secretCard = secretCard;
	}

	@Override
	public Entity getEventTarget() {
		return secretCard;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.SECRET_PLAYED;
	}

	@Override
	public Card getCard() {
		return secretCard;
	}
}
