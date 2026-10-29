package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

import java.util.Objects;

/**
 * {@code true} if the deck contains any cards that match the given {@link ConditionArg#CARD_FILTER} or {@link
 * ConditionArg#CARD}.
 */
public class DeckContainsCondition extends Condition {

	public DeckContainsCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			String card,
			EntityFilter cardFilter) {
		for (Card deckCard : player.getDeck()) {
			if ((cardFilter != null && cardFilter.matches(context, player, deckCard, source))
					|| (card != null && Objects.equals(card, deckCard.getCardId()))
					|| (cardFilter == null && card == null)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
