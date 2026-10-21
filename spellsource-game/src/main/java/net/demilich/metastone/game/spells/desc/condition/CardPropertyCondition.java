package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Objects;

/**
 * Returns {@code true} if the {@link ConditionArg#TARGET} or {@code target} is not {@code null} and meets the
 * requirements specified by {@link ConditionArg#CARD_TYPE}, {@link ConditionArg#HERO_CLASS}, {@link ConditionArg#RACE}
 * and the card ID in {@link ConditionArg#CARD}.
 * <p>
 * If {@link ConditionArg#CARDS} is specified, that line's requirement is satisfied if any of the cards in the array of
 * {@link ConditionArg#CARDS} matches the target's card ID.
 */
public final class CardPropertyCondition extends Condition {

	public CardPropertyCondition(ConditionDesc desc) {
		super(desc);
	}

	public static Condition create(String cardId) {
		var desc = new ConditionDesc(CardPropertyCondition.class);
		desc.put(ConditionArg.CARD, cardId);
		return desc.create();
	}

	public static Condition create(EntityReference target, String cardId) {
		var desc = new ConditionDesc(CardPropertyCondition.class);
		desc.put(ConditionArg.TARGET, target);
		desc.put(ConditionArg.CARD, cardId);
		return desc.create();
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			CardType cardType,
			String card,
			String[] cards,
			String heroClass,
			String race,
			Rarity rarity) {
		if (target == null) {
			return false;
		}
		var sourceCard = target.getSourceCard();
		if (sourceCard == null) {
			return false;
		}

		if (cardType != null && !GameLogic.isCardType(sourceCard.getCardType(), cardType)) {
			return false;
		}

		if (card != null && !sourceCard.getCardId().contains(card)) {
			return false;
		}

		if (cards != null && cards.length > 0) {
			var found = false;
			for (var i = 0; i < cards.length; i++) {
				if (Objects.equals(sourceCard.getCardId(), cards[i])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}

		if (heroClass != null && !sourceCard.hasHeroClass(heroClass)) {
			return false;
		}

		if (race != null && !Race.hasRace(context, sourceCard, race)) {
			return false;
		}

		if (rarity != null && !GameLogic.isRarity(sourceCard.getRarity(), rarity)) {
			return false;
		}

		return true;
	}

}
