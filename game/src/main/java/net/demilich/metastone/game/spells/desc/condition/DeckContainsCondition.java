package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;
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

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var cardId = (String) desc.get(ConditionArg.CARD);
		var cardFilter = (EntityFilter) desc.get(ConditionArg.CARD_FILTER);
		for (Card card : player.getDeck()) {
			if (cardFilter == null || cardFilter.matches(context, player, card, source) || Objects.equals(cardId, card.getCardId())) {
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
