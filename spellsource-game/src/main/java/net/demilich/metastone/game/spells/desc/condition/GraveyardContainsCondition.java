package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

/**
 * {@code true} if the graveyard contains an {@link Actor} that {@link Actor#diedOnBattlefield()} with the card ID from
 * {@link ConditionArg#CARD} or the {@code target}'s source card.
 */
public class GraveyardContainsCondition extends Condition {

	public GraveyardContainsCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			String card) {
		String cardId = card != null ? card : target.getSourceCard().getCardId();
		for (var deadEntity : player.getGraveyard()) {
			if (deadEntity instanceof Actor actor && deadEntity.diedOnBattlefield()) {
				if (actor.getSourceCard().getCardId().equalsIgnoreCase(cardId)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}
}
