package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

/**
 * A card was shuffled.
 * <p>
 * When {@link #isExtraCopy()} is {@code true}, this indicates the card being shuffled is itself a copy of a card being
 * shuffled into the deck, making it possible to not trigger off of shuffled extra cards.
 */
public class ShuffledEvent extends CardEvent {

	private final boolean extraCopy;

	public ShuffledEvent(GameContext context, int targetPlayerId, int sourcePlayerId, boolean extraCopy, Entity target, Card card) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.CARD_SHUFFLED, context, targetPlayerId, sourcePlayerId, card, target);
		this.extraCopy = extraCopy;
	}

	public ShuffledEvent(GameContext context, int targetPlayerId, int sourcePlayerId, boolean extraCopy, Card card) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.CARD_SHUFFLED, context, targetPlayerId, sourcePlayerId, card);
		this.extraCopy = extraCopy;
	}

	public boolean isExtraCopy() {
		return extraCopy;
	}
}

