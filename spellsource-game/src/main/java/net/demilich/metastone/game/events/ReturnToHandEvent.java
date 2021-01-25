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
public final class ReturnToHandEvent extends CardEvent {

	public ReturnToHandEvent(GameContext context, int playerId, Card card, Entity originalTarget) {
		super(com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.RETURNED_TO_HAND, context, originalTarget.getOwner(), playerId, card, originalTarget);
	}
}
