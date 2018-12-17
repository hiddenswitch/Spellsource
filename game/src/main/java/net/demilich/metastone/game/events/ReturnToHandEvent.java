package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

/**
 * Encapsulates the information related to returning a target to the hand.
 *
 * <ul>
 * <li>{@code eventTarget} is the original target that was returned</li>
 * <li>{@code eventSource} is the card entity that is now inside the player's hand. When this event is raised,
 * sub-spells have already been cast on it.</li>
 * </ul>
 */
public final class ReturnToHandEvent extends GameEvent implements HasCard {

	private static final long serialVersionUID = -6731150026791817283L;
	private final Card card;
	private final Entity originalTarget;

	public ReturnToHandEvent(GameContext context, int playerId, Card card, Entity originalTarget) {
		super(context, originalTarget.getOwner(), playerId);
		this.card = card;
		this.originalTarget = originalTarget;
	}

	@Override
	public Entity getEventTarget() {
		return originalTarget;
	}

	@Override
	public Entity getEventSource() {
		return card;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.RETURNED_TO_HAND;
	}

	@Override
	public Card getCard() {
		return card;
	}
}
