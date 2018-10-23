package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Returns {@code true} if the {@link ConditionArg#TARGET} or {@code target} is not {@code null} and meets the
 * requirements specified by {@link ConditionArg#CARD_TYPE}, {@link ConditionArg#HERO_CLASS}, {@link ConditionArg#RACE}
 * and the card ID in {@link ConditionArg#CARD}.
 */
public final class CardPropertyCondition extends Condition {

	public CardPropertyCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		target = desc.containsKey(ConditionArg.TARGET) ? context.resolveSingleTarget(player, source, (EntityReference) desc.get(ConditionArg.TARGET)) : target;

		if (target == null) {
			return false;
		}

		Card card = target.getSourceCard();

		if (card == null) {
			return false;
		}

		CardType cardType = (CardType) desc.get(ConditionArg.CARD_TYPE);
		if (cardType != null && !card.getCardType().isCardType(cardType)) {
			return false;
		}

		String cardId = (String) desc.get(ConditionArg.CARD);
		if (cardId != null && !card.getCardId().contains(cardId)) {
			return false;
		}

		HeroClass heroClass = (HeroClass) desc.get(ConditionArg.HERO_CLASS);
		if (heroClass != null && !card.hasHeroClass(heroClass)) {
			return false;
		}

		Race race = (Race) desc.get(ConditionArg.RACE);
		if (race != null && !card.getRace().hasRace(race)) {
			return false;
		}

		return true;
	}

}
