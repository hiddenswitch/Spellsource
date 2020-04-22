package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.HasCard;

/**
 * A secret was played.
 */
public class SecretPlayedEvent extends BasicGameEvent implements HasCard {

	private final Card secretCard;

	public SecretPlayedEvent(GameContext context, int playerId, Card secretCard) {
		super(GameEvent.EventTypeEnum.SECRET_PLAYED, true, context, context.getPlayer(playerId), context.getPlayer(playerId), secretCard);
		this.secretCard = secretCard;
	}

	@Override
	public Card getSourceCard() {
		return secretCard;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s played a Secret (Casts when %s performs the secret condition written on it)",
				context.getPlayer(playerId).getName(), context.getOpponent(context.getPlayer(playerId)));
	}
}
