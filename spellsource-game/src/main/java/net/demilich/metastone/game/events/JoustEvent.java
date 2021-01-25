package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.HasCard;

/**
 * A joust event describes which cards were drawn and who won a joust from a point of view of a particular player.
 */
public final class JoustEvent extends GameEvent implements HasCard {

	private final boolean won;
	private final Card ownCard;
	private final Card opponentCard;

	public JoustEvent(GameContext context, int playerId, boolean won, Card ownCard, Card opponentCard) {
		super(context, ownCard, ownCard, -1, playerId);
		this.won = won;
		this.ownCard = ownCard;
		this.opponentCard = opponentCard;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.JOUST;
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
