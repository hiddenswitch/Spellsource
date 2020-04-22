package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * {@code true} if the graveyard contains an {@link Actor} that {@link Actor#diedOnBattlefield()} with the card ID from
 * {@link ConditionArg#CARD} or the {@code target}'s source card.
 */
public class GraveyardContainsCondition extends Condition {

	public GraveyardContainsCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var cardId = (String) desc.getOrDefault(ConditionArg.CARD, target.getSourceCard().getCardId());

		for (var deadEntity : player.getGraveyard()) {
			Card card;
			if (deadEntity instanceof Actor && deadEntity.diedOnBattlefield()) {
				var actor = (Actor) deadEntity;
				card = actor.getSourceCard();
			} else {
				continue;
			}

			if (card.getCardId().equalsIgnoreCase(cardId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}
}
