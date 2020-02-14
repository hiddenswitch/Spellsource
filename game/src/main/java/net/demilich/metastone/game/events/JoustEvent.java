package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

/**
 * A joust event describes which cards were drawn and who won a joust from a point of view of a particular player.
 */
public class JoustEvent extends GameEvent implements HasCard {

	private final boolean won;
	private final Card ownCard;
	private final Card opponentCard;

	public JoustEvent(GameContext context, int playerId, boolean won, Card ownCard, Card opponentCard) {
		super(context, playerId, -1);
		this.won = won;
		this.ownCard = ownCard;
		this.opponentCard = opponentCard;
	}

	@Override
	public Entity getEventTarget() {
		return getOwnCard();
	}

	@Override
	public EventTypeEnum getEventType() {
		return EventTypeEnum.JOUST;
	}

	public boolean isWon() {
		return won;
	}

	public Card getOwnCard() {
		return ownCard;
	}

	public Card getOpponentCard() {
		return opponentCard;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	@Override
	public Card getSourceCard() {
		return ownCard;
	}
}
